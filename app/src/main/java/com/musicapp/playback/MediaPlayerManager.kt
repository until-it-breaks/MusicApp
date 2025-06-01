package com.musicapp.playback

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.first

data class PlaybackUiState(
    val currentPlayingTrackId: Long? = null,
    val currentTrack: TrackModel? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val playbackError: String? = null,
    val playbackQueue: List<TrackModel> = emptyList(),
    val currentQueueIndex: Int = -1,
    val currentPositionMs: Long = 0L,
    val trackDurationMs: Long = 30000L // Default preview duration, can be updated
)

@UnstableApi
class MediaPlayerManager(
    private val appContext: Context
) : Player.Listener {
    private var exoPlayer: ExoPlayer? = null
    private val _isExoPlayerReady = MutableStateFlow(false)

    private val _playbackState = MutableStateFlow(PlaybackUiState())
    val playbackState: StateFlow<PlaybackUiState> = _playbackState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    private var positionUpdateJob: Job? = null

    fun setExoPlayerInstance(player: ExoPlayer?) {
        if (this.exoPlayer == player) {
            // Same player
            return
        }

        this.exoPlayer?.removeListener(this)
        _isExoPlayerReady.value = (player != null)

        this.exoPlayer = player
        Log.d("MediaPlayerManager", "ExoPlayer instance set by service.")

        this.exoPlayer?.addListener(this)

        if (exoPlayer != null) {
            _playbackState.update { currentState ->
                val newIsPlaying = exoPlayer?.isPlaying == true
                val newLoading = exoPlayer?.playbackState == Player.STATE_BUFFERING
                val currentMediaItem = exoPlayer?.currentMediaItem
                val currentTrackId = currentMediaItem?.mediaId?.toLongOrNull()
                val currentTrackInQueue = currentState.playbackQueue.find { it.id == currentTrackId }
                val newIndex = exoPlayer?.currentMediaItemIndex ?: -1

                currentState.copy(
                    isPlaying = newIsPlaying,
                    isLoading = newLoading,
                    currentPlayingTrackId = currentTrackInQueue?.id,
                    currentTrack = currentTrackInQueue,
                    currentQueueIndex = newIndex,
                    currentPositionMs = exoPlayer?.currentPosition ?: 0L,
                    // default duration, can be updated
                    trackDurationMs = 30000L
                )
            }
            if (exoPlayer?.isPlaying == true) {
                startPositionUpdates()
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        Log.d("MediaPlayerManager", "Manager: ExoPlayer playback state changed: $playbackState")
        _playbackState.update { currentState ->
            val newIsLoading = playbackState == Player.STATE_BUFFERING
            currentState.copy(isLoading = newIsLoading)
        }
        if (playbackState == Player.STATE_ENDED) {
            Log.d("MediaPlayerManager", "ExoPlayer: Track ended naturally.")
            // exoplayer will automatically move to next item if available in its queue.
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d("MediaPlayerManager", "Manager: ExoPlayer isPlaying changed: $isPlaying")
        _playbackState.update { it.copy(isPlaying = isPlaying) }
        if (isPlaying) {
            startPositionUpdates()
        } else {
            stopPositionUpdates()
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val trackId = mediaItem?.mediaId?.toLongOrNull()
        val originalTrackModel = _playbackState.value.playbackQueue.find { it.id == trackId }
        val newIndex = exoPlayer?.currentMediaItemIndex ?: -1

        Log.d("MediaPlayerManager", "Manager: Media item transition to ${originalTrackModel?.title}, new index: $newIndex")
        _playbackState.update {
            it.copy(
                currentPlayingTrackId = originalTrackModel?.id,
                currentTrack = originalTrackModel,
                currentQueueIndex = newIndex,
                currentPositionMs = 0L, // Reset position on new track
                // default duration, can be updated
                trackDurationMs = 30000L
            )
        }
    }

    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
        Log.e("MediaPlayerManager", "Manager: ExoPlayer error: ${error.message}", error)
        _playbackState.update {
            it.copy(
                isPlaying = false,
                currentPlayingTrackId = null,
                currentTrack = null,
                isLoading = false,
                playbackError = "Playback error: ${error.message}"
            )
        }
        stopPositionUpdates()
    }

    /**
     * Plays a new track, clearing the queue and setting this as the first.
     */
    fun playTrack(track: TrackModel) {
        setPlaybackQueue(listOf(track), 0) // Sets a new queue with just this track and plays it
    }

    /**
     * Toggles playback for a specific track.
     * If the track is currently playing, it pauses.
     * If the track is currently paused, it resumes.
     * If a different track is playing, it stops and starts playing the new track.
     */
    fun togglePlayback(track: TrackModel) {
        val currentState = _playbackState.value
        val currentTrackId = currentState.currentPlayingTrackId

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
                Log.d("MediaPlayerManager", "No next track available.")
                // stop when reach the end of queue
                stop()
            }
        }
    }

    /**
     * Plays the previous track in the queue, if available.
     */
    fun playPrevious() {
        scope.launch {
            if (exoPlayer?.hasPreviousMediaItem() == true) {
                exoPlayer?.seekToPreviousMediaItem()
                _playbackState.update { it.copy(isLoading = true, playbackError = null) }
            } else {
                Log.d("MediaPlayerManager", "No previous track available.")
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

        _playbackState.update {
            it.copy(
                isPlaying = false,
                currentPlayingTrackId = null,
                currentTrack = null,
                isLoading = false,
                currentQueueIndex = -1,
                playbackQueue = emptyList(),
                currentPositionMs = 0L,
                trackDurationMs = 30000L
            )
        }

        val serviceIntent = Intent(appContext, MediaPlaybackService::class.java)
        appContext.stopService(serviceIntent)
        Log.d("MediaPlayerManager", "Stopped playback and MediaPlaybackService.")
    }

    /**
     * Sets a new playback queue for ExoPlayer and immediately starts playing from the specified index.
     */
    fun setPlaybackQueue(newQueue: List<TrackModel>, startIndex: Int) {
        scope.launch {
            Log.d("MediaPlayerManager", "Setting new playback queue. Size: ${newQueue.size}, Start Index: $startIndex")

            stopPositionUpdates()

            _playbackState.update {
                it.copy(
                    playbackQueue = newQueue,
                    currentQueueIndex = startIndex,
                    isPlaying = false,
                    isLoading = true,
                    playbackError = null,
                    currentPlayingTrackId = null,
                    currentTrack = null,
                    currentPositionMs = 0L,
                    trackDurationMs = 30000L
                )
            }
            val mediaItems = newQueue.map { track ->
                MediaItem.Builder()
                    .setMediaId(track.id.toString())
                    .setUri(track.previewUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.contributors.joinToString(", ") { it.name })
                            .setArtworkUri(track.bigPictureUri)
                            .build()
                    )
                    .build()
            }

            val serviceIntent = Intent(appContext, MediaPlaybackService::class.java)
            appContext.startService(serviceIntent)
            Log.d("MediaPlayerManager", "Started MediaPlaybackService for new queue.")

            // race condition, wait for service to bind (Pain.)
            _isExoPlayerReady.first { it }

            exoPlayer?.apply {
                setMediaItems(mediaItems, startIndex, 0L)
                prepare()
                play()
            } ?: run {
                Log.e("MediaPlayerManager", "ExoPlayer not available when trying to set queue. Service might not be ready.")
            }
        }
    }

    /**
     * Adds a single track to the end of the current playback queue.
     * If nothing is playing, it starts playback of this track.
     */
    fun addTrackToQueue(track: TrackModel) {
        scope.launch {
            val currentQueue = _playbackState.value.playbackQueue.toMutableList()

            if (currentQueue.isEmpty()) {
                Log.d("MediaPlayerManager", "Queue was empty. Playing new track: ${track.title}")
                playTrack(track)
            } else {
                currentQueue.add(track)
                _playbackState.update { it.copy(playbackQueue = currentQueue) }

                val mediaItemToAdd = MediaItem.Builder()
                    .setMediaId(track.id.toString())
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
                Log.d("MediaPlayerManager", "Added track '${track.title}' to end of queue. New queue size: ${currentQueue.size}")
            }
        }
    }

    /**
     * Clears the entire playback queue and stops current playback.
     */
    fun clearQueue() {
        stop()
        Log.d("MediaPlayerManager", "Playback queue cleared.")
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (_playbackState.value.isPlaying) {
                exoPlayer?.let { player ->
                    _playbackState.update {
                        it.copy(currentPositionMs = player.currentPosition.toLong())
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    fun release() {
        Log.d("MediaPlayerManager", "Releasing ExoPlayer resources.")
        stopPositionUpdates()
        exoPlayer?.release()
        exoPlayer = null
        _playbackState.value = PlaybackUiState()
        val serviceIntent = Intent(appContext, MediaPlaybackService::class.java)
        appContext.stopService(serviceIntent)
    }
}