package com.musicapp.playback

import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Base ViewModel for screens that interact with music playback.
 * Provides access to MediaPlayerManager and common playback actions.
 *
 * @param mediaPlayerManager The MediaPlayerManager instance, in this case injected via Koin.
 */
@UnstableApi
open class BasePlaybackViewModel
    (
    protected val mediaPlayerManager: MediaPlayerManager
) : ViewModel() {

    val playbackUiState: StateFlow<PlaybackUiState> = mediaPlayerManager.playbackState

    fun addTrackToQueue(track: TrackModel) {
        mediaPlayerManager.addTrackToQueue(track)
    }

    fun removeTrackFromQueue(track: TrackModel){
        mediaPlayerManager.removeTrackFromQueue(track)
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

    fun toggleShuffleMode(){
        mediaPlayerManager.toggleShuffleMode()
    }

    fun stopMusic() {
        mediaPlayerManager.stop()
    }

    fun playNextTrack() {
        mediaPlayerManager.playNext()
    }

    fun playPreviousTrack() {
        mediaPlayerManager.playPrevious()
    }

    fun toggleRepeatMode(){
        mediaPlayerManager.toggleRepeatMode()
    }

    fun seekTo(positionMs: Long) {
        mediaPlayerManager.seekTo(positionMs)
    }
}