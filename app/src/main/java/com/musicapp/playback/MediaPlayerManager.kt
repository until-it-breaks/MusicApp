package com.musicapp.playback

import android.media.MediaPlayer
import android.util.Log
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
    val playbackError: String? = null
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
                    mp.stop()
                    mp.reset() // Reset for next use
                    _playbackState.update {
                        it.copy(
                            isPlaying = false,
                            currentPlayingTrackId = null,
                            currentTrack = null,
                            isLoading = false
                        )
                    }
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

    // Public API for controlling playback
    fun togglePlayback(track: TrackModel) {
        scope.launch {
            val currentState = _playbackState.value
            if (currentState.currentPlayingTrackId == track.id && currentState.isPlaying) {
                // Same track is playing, pause it
                pause()
            } else if (currentState.currentPlayingTrackId == track.id && !currentState.isPlaying) {
                // Same track is paused, resume it
                resume()
            } else {
                // Different track, or nothing playing, start new playback
                play(track)
            }
        }
    }

    private fun play(track: TrackModel) {
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
                    "Error setting data source or preparing MediaPlayer for ${track.previewUri.toString()}: ${e.message}",
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

    fun pause() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
                _playbackState.update { it.copy(isPlaying = false) }
            }
        }
    }

    fun resume() {
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
                    isLoading = false
                )
            }
        }
    }

    fun release() {
        Log.d("MediaPlayerManager", "Releasing MediaPlayer resources.")
        mediaPlayer?.release()
        mediaPlayer = null
        _playbackState.value = PlaybackUiState() // Reset state on release
    }
}