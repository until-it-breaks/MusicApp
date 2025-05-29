package com.musicapp.playback

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.currentRecomposeScope
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val currentPlayingTrackId: Long? = null,
    val currentTrack: TrackModel? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val playbackError: String? = null,
    val playbackQueue: List<TrackModel> = emptyList(),
    val currentQueueIndex: Int = -1
    // Add other properties like currentPosition, duration, mediaTitle, mediaArtist if needed
)

// You'll likely need an application context for MediaPlayer if you set data source from file
// For URL, it might not be strictly necessary, but good practice for future expansion.
// @Suppress("ForbiddenEntryPoint") // Suppress warning for Application context usage if necessary
// import android.content.Context

class MediaPlayerManager(
    // private val appContext: Context // If you need context for MediaPlayer initialization later
) {
    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow(PlaybackUiState())
    val playbackState: StateFlow<PlaybackUiState> = _playbackState.asStateFlow()

    // Using a separate scope for MediaPlayer callbacks/updates if they trigger coroutines
    private val scope = CoroutineScope(Dispatchers.Main) // Or Dispatchers.IO if heavy work

    init {
        initializeMediaPlayer()
    }

    private fun initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener { mp ->
                    mp.start()
                    _playbackState.update {
                        it.copy(
                            isPlaying = true,
                            isLoading = false,
                            playbackError = null
                        )
                    }
                }
                setOnCompletionListener { mp ->
                    Log.d("MediaPlayerManager", "Track completed. Playing next if available.")
                    mp.stop()
                    mp.reset() // Reset for next use
                    playNext() // Next in the queue
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("MediaPlayerManager", "MediaPlayer error: what=$what, extra=$extra")
                    mp.reset()
                    _playbackState.update {
                        it.copy(
                            isPlaying = false,
                            currentPlayingTrackId = null,
                            currentTrack = null,
                            isLoading = false,
                            playbackError = "Playback error: $what"
                        )
                    }
                    true // Indicates error was handled
                }
                // You might also add setOnBufferingUpdateListener for progress indicators
            }
        }
    }

    fun playNext() {
        templatePlay(1)
    }

    fun playPrevious() {
        templatePlay(-1)
    }

    private fun templatePlay(offSet: Int) {
        scope.launch {
            val currentState = _playbackState.value
            val nextIndex = currentState.currentQueueIndex + offSet

            if (currentState.playbackQueue.isEmpty()) {
                Log.d("MediaPlayerManager", "Queue is empty. Cannot play next/previous.")
                stop()
                return@launch // Exit coroutine
            }

            if (nextIndex >= 0 && nextIndex < currentState.playbackQueue.size) {
                val nextTrack = currentState.playbackQueue[nextIndex]
                _playbackState.update {
                    it.copy(currentQueueIndex = nextIndex)
                }
                playInternal(nextTrack)
            } else if (nextIndex >= currentState.playbackQueue.size){
                Log.d("MediaPlayerManager", "End of queue reached.")
                // No more tracks in queue, reset state
                stop()
            } else if (nextIndex < 0){
                Log.d("MediaPlayerManager", "Start of queue reached. Restarting first track.")
                _playbackState.update {
                    it.copy(currentQueueIndex = 0)
                }
                playInternal(currentState.playbackQueue[0])
            }
        }
    }

    // Public API for controlling playback
    fun togglePlayback(track: TrackModel) {
        scope.launch {
            val currentState = _playbackState.value
            val currentTrackId = currentState.currentPlayingTrackId
            if (currentTrackId == track.id) {
                if (currentState.isPlaying) {
                    // Same track is playing, pause it
                    pause()
                } else {
                    resume()
                }
            } else {
                // if the track id is different, start a new session, this will clear the queue
                playNewTrack(track)
            }
        }
    }

    private fun playNewTrack(track: TrackModel) {
        scope.launch {
            // Stop any existing playback and clear the queue
            stop() // This will clear the queue and reset current track/index

            // Set up the new queue with just this track
            _playbackState.update {
                it.copy(
                    playbackQueue = listOf(track),
                    currentQueueIndex = 0
                )
            }
            playInternal(track)
        }
    }

    private fun playInternal(track: TrackModel) {
        _playbackState.update {
            it.copy(
                currentPlayingTrackId = track.id,
                currentTrack = track,
                isPlaying = false, // Will be true on prepare
                isLoading = true,
                playbackError = null
            )
        }
        mediaPlayer?.apply {
            try {
                reset() // Reset to idle state
                setDataSource(track.previewUri.toString()) // Set the URL
                prepareAsync() // Prepare in background
            } catch (e: Exception) {
                Log.e(
                    "MediaPlayerManager",
                    "Error setting data source or preparing MediaPlayer for ${track.previewUri}: ${e.message}",
                    e
                )
                _playbackState.update {
                    it.copy(
                        isPlaying = false,
                        isLoading = false,
                        currentPlayingTrackId = null,
                        currentTrack = null,
                        playbackError = "Failed to play track: ${e.message}"
                    )
                }
            }
        } ?: run {
            Log.e("MediaPlayerManager", "MediaPlayer not initialized. Attempting to re-initialize.")
            // Try to re-initialize if null (e.g., after release)
            initializeMediaPlayer()
            mediaPlayer?.let { player ->
                try {
                    player.reset()
                    player.setDataSource(track.previewUri.toString())
                    player.prepareAsync()
                } catch (e: Exception) {
                    Log.e("MediaPlayerManager", "Failed to re-initialize and play: ${e.message}", e)
                    _playbackState.update {
                        it.copy(
                            isPlaying = false,
                            isLoading = false,
                            currentPlayingTrackId = null,
                            currentTrack = null,
                            playbackError = "Failed to play track: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    private fun pause() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                _playbackState.update { it.copy(isPlaying = false) }
            }
        }
    }

    private fun resume() {
        mediaPlayer?.apply {
            if (!isPlaying) {
                start()
                _playbackState.update { it.copy(isPlaying = true) }
            }
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            reset() // Reset to idle state
            _playbackState.update {
                it.copy(
                    isPlaying = false,
                    currentPlayingTrackId = null,
                    currentTrack = null,
                    isLoading = false,
                    currentQueueIndex = -1,
                    playbackQueue = emptyList()
                )
            }
        }
    }

    /**
     * Adds a track to the end of the current playback queue.
     * If nothing is playing, it will start playing this track immediately.
     */
    fun addTrackToQueue(track: TrackModel) {
        scope.launch {
            scope.launch {
                val currentState = _playbackState.value
                val updatedQueue = currentState.playbackQueue + track
                _playbackState.update {
                    it.copy(playbackQueue = updatedQueue)
                }

                // If the queue was empty before adding this track, start playing it
                if (currentState.currentTrack == null && currentState.playbackQueue.isEmpty()) {
                    _playbackState.update {
                        it.copy(currentQueueIndex = 0) // Set index to the newly added track
                    }
                    playInternal(track) // Play the first track in the queue
                }
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

    fun release() {
        Log.d("MediaPlayerManager", "Releasing MediaPlayer resources.")
        mediaPlayer?.release()
        mediaPlayer = null
        _playbackState.value = PlaybackUiState() // Reset state on release
    }
}