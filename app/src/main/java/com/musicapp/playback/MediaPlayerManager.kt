package com.musicapp.playback

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.musicapp.data.repositories.TrackHistoryRepository
import com.musicapp.data.models.TrackModel
import com.musicapp.auth.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MediaPlaybackManager"

data class PlaybackUiState(
    val currentQueueItemId: Long? = null,
    val currentQueueItem: QueueItem? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val playbackError: String? = null,
    val playbackQueue: List<QueueItem> = emptyList(),
    val currentQueueIndex: Int = -1,
    val currentPositionMs: Long = 0L,
    val trackDurationMs: Long = 30000L,
    val isShuffleModeEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
)

data class QueueItem(
    val id: Long,
    val track: TrackModel
)

enum class RepeatMode {
    OFF,
    ON,
    ONE,
    ONCE
}

@UnstableApi
class MediaPlayerManager(
    private val appContext: Context,
    private val authManager: AuthManager,
    private val trackHistoryRepository: TrackHistoryRepository
) : Player.Listener {

    private var exoPlayer: ExoPlayer? = null
    private val _isExoPlayerReady = MutableStateFlow(false)
    private val _playbackState = MutableStateFlow(PlaybackUiState())
    val playbackState: StateFlow<PlaybackUiState> = _playbackState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var positionUpdateJob: Job? = null
    private var _nextQueueId: Long = 0L

    fun setExoPlayerInstance(player: ExoPlayer?) {
        if (this.exoPlayer == player) {
            // Same player
            return
        }

        this.exoPlayer?.removeListener(this)
        _isExoPlayerReady.value = (player != null)

        this.exoPlayer = player
        Log.d(TAG, "ExoPlayer instance set by service.")

        this.exoPlayer?.addListener(this)

        if (exoPlayer != null) {
            _playbackState.update { currentState ->
                val newIsPlaying = exoPlayer?.isPlaying == true
                val newLoading = exoPlayer?.playbackState == Player.STATE_BUFFERING
                val currentMediaItem = exoPlayer?.currentMediaItem
                val currentQueueItemId = currentMediaItem?.mediaId?.toLongOrNull()
                val currentQueueItem = currentState.playbackQueue.find { it.id == currentQueueItemId }
                val newIndex = currentState.playbackQueue.indexOf(currentQueueItem)

                currentState.copy(
                    isPlaying = newIsPlaying,
                    isLoading = newLoading,
                    currentQueueItemId = currentQueueItem?.id,
                    currentQueueItem = currentQueueItem,
                    currentQueueIndex = newIndex,
                    currentPositionMs = exoPlayer?.currentPosition ?: 0L,
                    // default duration, can be updated
                    trackDurationMs = 30000L,
                    isShuffleModeEnabled = currentState.isShuffleModeEnabled
                )
            }
            if (exoPlayer?.isPlaying == true) {
                startPositionUpdates()
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        Log.d(TAG, "Manager: ExoPlayer playback state changed: $playbackState")
        _playbackState.update { currentState ->
            val newIsLoading = playbackState == Player.STATE_BUFFERING
            currentState.copy(isLoading = newIsLoading)
        }
        if (playbackState == Player.STATE_ENDED) {
            Log.d(TAG, "ExoPlayer: Queue ended naturally.")
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d(TAG, "Manager: ExoPlayer isPlaying changed: $isPlaying")
        _playbackState.update { it.copy(isPlaying = isPlaying) }
        if (isPlaying) {
            startPositionUpdates()
        } else {
            stopPositionUpdates()
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val currentState = _playbackState.value
        val previousQueueItemId = currentState.currentQueueItemId
        val newQueueItemId = mediaItem?.mediaId?.toLongOrNull()
        val newQueueItem = currentState.playbackQueue.find { it.id == newQueueItemId }
        val newIndex = currentState.playbackQueue.indexOf(newQueueItem)

        authManager.userId.value?.let {
            scope.launch {
                withContext(Dispatchers.IO) {
                    if (newQueueItem != null) {
                        trackHistoryRepository.addTrackToTrackHistory(it, newQueueItem.track)
                    }
                }
            }
        }

        if (currentState.repeatMode == RepeatMode.ONCE && previousQueueItemId != newQueueItemId) {
            playPrevious()
            pause()
            return
        }

        Log.d(TAG, "Manager: Media item transition to ${newQueueItem?.track?.title}, new index: $newIndex")
        val startPosition = if (currentState.currentQueueItemId == newQueueItem?.id) {
            currentState.currentPositionMs
        } else 0L
        _playbackState.update {
            it.copy(
                currentQueueItemId = newQueueItemId,
                currentQueueItem = newQueueItem,
                currentQueueIndex = newIndex,
                currentPositionMs = startPosition,
                // default duration, can be updated
                trackDurationMs = 30000L
            )
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        Log.d(TAG, "Manager: onPositionDiscontinuity: Old ${oldPosition.positionMs}, New ${newPosition.positionMs}, Reason: $reason")
        _playbackState.update { it.copy(currentPositionMs = newPosition.positionMs) }
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        _playbackState.update { it.copy(isShuffleModeEnabled = shuffleModeEnabled) }
        Log.d(TAG, "Manager: ExoPlayer shuffle mode changed: $shuffleModeEnabled")
    }

    override fun onRepeatModeChanged(@Player.RepeatMode repeatMode: Int) {
        Log.d(TAG, "Manager: ExoPlayer repeat mode changed to $repeatMode")
    }

    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
        Log.e(TAG, "Manager: ExoPlayer error: ${error.message}", error)
        _playbackState.update {
            it.copy(
                isPlaying = false,
                currentQueueItemId = null,
                currentQueueItem = null,
                isLoading = false,
                playbackError = "Playback error: ${error.message}"
            )
        }
        stopPositionUpdates()
    }

    /**
     * Plays a new track, clearing the queue and setting this as the first.
     */
    suspend fun playTrack(track: TrackModel) {
        setPlaybackQueue(listOf(track), 0) // Sets a new queue with just this track and plays it
    }

    /**
     * Toggles playback for a specific track.
     * If the track is currently playing, it pauses.
     * If the track is currently paused, it resumes.
     * If a different track is playing, it stops and starts playing the new track.
     */
    suspend fun togglePlayback(track: TrackModel) {
        val currentState = _playbackState.value
        val currentTrackId = currentState.currentQueueItem?.track?.id

        if (currentTrackId == track.id) {
            if (currentState.isPlaying) {
                pause()
            } else {
                resume()
            }
        } else {
            // New track, clear queue and play this one
            playTrack(track)
        }
    }

    /**
     * Plays the next track in the queue, if available.
     */
    fun playNext() {
        scope.launch {
            if (exoPlayer?.hasNextMediaItem() == true) {
                exoPlayer?.seekToNextMediaItem()
                _playbackState.update { it.copy(isLoading = true, playbackError = null) }
            } else {
                Log.d(TAG, "No next track available.")
                // stop when reach the end of queue
                seekTo(0L)
                pause()
            }
        }
    }

    /**
     * Plays the previous track in the queue, if available.
     */
    fun playPrevious() {
        scope.launch {
            val hasPrevious = exoPlayer?.hasPreviousMediaItem() == true
            if (hasPrevious) {
                exoPlayer?.seekToPreviousMediaItem()
                _playbackState.update { it.copy(isLoading = true, playbackError = null) }
            } else {
                Log.d(TAG, "No previous track available.")
                // restart if reach the start of queue
                seekTo(0L)
            }
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun stop() {
        stopPositionUpdates()
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        exoPlayer?.seekTo(0)

        _playbackState.value = PlaybackUiState()

        val serviceIntent = Intent(appContext, MediaPlaybackService::class.java)
        appContext.stopService(serviceIntent)
        Log.d(TAG, "Stopped playback and MediaPlaybackService.")
    }

    /**
     * Sets a new playback queue for ExoPlayer and immediately starts playing from the specified index.
     */
    suspend fun setPlaybackQueue(newTracks: List<TrackModel>, startIndex: Int) {
        val newQueueItems = newTracks.map { track ->
            QueueItem(id = _nextQueueId++, track = track)
        }
        _playbackState.update {
            it.copy(
                playbackQueue = newQueueItems,
                currentQueueIndex = startIndex,
                isPlaying = false,
                isLoading = true,
                playbackError = null,
                currentQueueItemId = newQueueItems.getOrNull(startIndex)?.id,
                currentQueueItem = newQueueItems.getOrNull(startIndex),
                currentPositionMs = 0L,
                trackDurationMs = 30000L
            )
        }
        setPlaybackQueueInternal(newQueueItems, startIndex)
    }

    /**
     * Toggles shuffle mode on or off.
     * When turned on, it shuffles the queue keeping the current track first.
     * When turned off, it restores the original queue and tries to find the current track's index.
     */
    fun toggleShuffleMode() {
        val currentState = _playbackState.value
        val newShuffleMode = !currentState.isShuffleModeEnabled
        exoPlayer?.shuffleModeEnabled = newShuffleMode
        Log.d(TAG, "Shuffle mode toggled to: $newShuffleMode")
    }

    private suspend fun setPlaybackQueueInternal(
        newQueue: List<QueueItem>,
        startIndex: Int,
        currentTime: Long = 0L
    ) {
        Log.d(TAG, "Internal: Setting playback queue. Size: ${newQueue.size}, Start Index: $startIndex, Shuffle: ${_playbackState.value.isShuffleModeEnabled}")

        stopPositionUpdates()

        val mediaItems = newQueue.map { queueItem ->
            MediaItem.Builder()
                .setMediaId(queueItem.id.toString())
                .setUri(queueItem.track.previewUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(queueItem.track.title)
                        .setArtist(queueItem.track.contributors.joinToString(", ") { it.name })
                        .setArtworkUri(queueItem.track.bigPictureUri)
                        .build()
                )
                .build()
        }

        if (exoPlayer == null) {
            val serviceIntent = Intent(appContext, MediaPlaybackService::class.java)
            appContext.startService(serviceIntent)
            Log.d(TAG, "Started MediaPlaybackService for new queue.")
        } else {
            Log.d(TAG, "Updated MediaPlaybackService for new queue.")
        }

        // race condition, wait for service to bind
        _isExoPlayerReady.first { it }

        exoPlayer?.apply {
            setMediaItems(mediaItems, startIndex, currentTime)
            prepare()
            play()
        } ?: run {
            Log.e(TAG, "ExoPlayer not available when trying to set queue. Service might not be ready.")
        }
    }

    /**
     * Adds a single track to the end of the current playback queue.
     * If nothing is playing, it starts playback of this track.
     */
    fun addTrackToQueue(track: TrackModel) {
        scope.launch {
            val currentState = _playbackState.value
            val currentQueue = currentState.playbackQueue.toMutableList()
            val newQueueItem = QueueItem(id = _nextQueueId++, track = track)

            if (currentQueue.isEmpty()) {
                Log.d(TAG, "Queue was empty. Playing new track: ${track.title}")
                playTrack(track)
            } else {
                currentQueue.add(newQueueItem)
                _playbackState.update { it.copy(playbackQueue = currentQueue) }

                val mediaItemToAdd = MediaItem.Builder()
                    .setMediaId(newQueueItem.id.toString())
                    .setUri(track.previewUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.contributors.joinToString(", ") { it.name })
                            .setArtworkUri(track.bigPictureUri)
                            .build()
                    )
                    .build()

                exoPlayer?.addMediaItem(mediaItemToAdd)
                Log.d(TAG, "Added track '${track.title}' to end of queue. New queue size: ${currentQueue.size}")
            }
        }
    }

    fun removeTrackFromQueue(queueItem: QueueItem) {
        scope.launch {
            val currentQueue = _playbackState.value.playbackQueue.toMutableList()
            val index = currentQueue.indexOf(queueItem)
            currentQueue.remove(queueItem)
            _playbackState.update { it.copy(playbackQueue = currentQueue) }

            exoPlayer?.removeMediaItem(index)
            Log.d(TAG, "Removed track '${queueItem.track.title}' from queue. New queue size: ${currentQueue.size}")
        }
    }

    /**
     * Clears the entire playback queue but the current track.
     */
    fun clearQueue() {
        scope.launch {
            val currentState = _playbackState.value
            val currentTrack = currentState.currentQueueItem.let { queueItem ->
                queueItem!!.track
            }
            val currentPositionMs = currentState.currentPositionMs
            val isPlaying = currentState.isPlaying

            _nextQueueId = 0
            setPlaybackQueue(listOf(currentTrack), 0)
            seekTo(currentPositionMs)
            if (isPlaying){
                resume()
            } else {
                pause()
            }
            Log.d(TAG, "Playback queue cleared.")
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
        Log.d(TAG, "CurrentPositionMs set to $positionMs")
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (_playbackState.value.isPlaying) {
                exoPlayer?.let { player ->
                    _playbackState.update { it.copy(currentPositionMs = player.currentPosition.toLong()) }
                }
                delay(1000L)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    fun toggleRepeatMode() {
        val currentState = _playbackState.value

        val nextRepeatMode = when (currentState.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ON
            RepeatMode.ON -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ONCE
            RepeatMode.ONCE -> RepeatMode.OFF
        }

        exoPlayer?.repeatMode = when (nextRepeatMode) {
            RepeatMode.OFF, RepeatMode.ONCE -> Player.REPEAT_MODE_OFF
            RepeatMode.ON -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }

        Log.d(TAG, "Repeat mode changed to: $nextRepeatMode")
        _playbackState.update { it.copy(repeatMode = nextRepeatMode) }
    }

    fun release() {
        Log.d(TAG, "Releasing ExoPlayer resources and resetting state.")
        stopPositionUpdates()
        exoPlayer?.release()
        exoPlayer = null
        _isExoPlayerReady.value = false
        _playbackState.value = PlaybackUiState()
        _nextQueueId = 0L
        val serviceIntent = Intent(appContext, MediaPlaybackService::class.java)
        appContext.stopService(serviceIntent)
    }
}