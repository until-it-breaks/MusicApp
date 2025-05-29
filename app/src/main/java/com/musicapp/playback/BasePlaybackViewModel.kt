package com.musicapp.playback

import androidx.lifecycle.ViewModel
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Base ViewModel for screens that interact with music playback.
 * Provides access to MediaPlayerManager and common playback actions.
 *
 * @param mediaPlayerManager The MediaPlayerManager instance, in this case injected via Koin.
 */
open class BasePlaybackViewModel(
    protected val mediaPlayerManager: MediaPlayerManager
) : ViewModel() {

    val playbackUiState: StateFlow<PlaybackUiState> = mediaPlayerManager.playbackState

    fun addTrackToQueue(track: TrackModel) {
        mediaPlayerManager.addTrackToQueue(track)
    }

    fun setPlaybackQueue(queue: List<TrackModel>, index: Int = 0) {
        mediaPlayerManager.setPlaybackQueue(queue, index)
    }

    fun clearPlaybackQueue() {
        mediaPlayerManager.clearQueue()
    }


    /**
     * Toggles playback for a given track.
     * If the track is currently playing, it pauses.
     * If the track is paused, it resumes.
     * If a different track, it stops current playback and starts playing the new track,
     * replacing the current queue with just this track.
     */
    fun togglePlayback(track: TrackModel) {
        mediaPlayerManager.togglePlayback(track)
    }

    fun stopMusic() {
        mediaPlayerManager.stop()
    }

    /**
     * Plays the next track in the queue.
     */
    fun playNextTrack() {
        mediaPlayerManager.playNext()
    }

    /**
     * Plays the previous track in the queue.
     */
    fun playPreviousTrack() {
        mediaPlayerManager.playPrevious()
    }
}