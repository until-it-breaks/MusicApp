package com.musicapp.ui.screens.main

import androidx.lifecycle.ViewModel
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.playback.PlaybackUiState
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    private val mediaPlayerManager: MediaPlayerManager
) : ViewModel() {

    val playbackUiState: StateFlow<PlaybackUiState> = mediaPlayerManager.playbackState

    fun togglePlayback(track: TrackModel) {
        mediaPlayerManager.togglePlayback(track)
    }

    fun stopMusic() {
        mediaPlayerManager.stop()
    }

    fun addTrackToPlaylist(track: TrackModel) {
        // TODO: Implement logic to add track to a playlist or queue
    }
}