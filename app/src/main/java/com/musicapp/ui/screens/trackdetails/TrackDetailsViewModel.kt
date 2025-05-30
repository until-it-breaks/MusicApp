package com.musicapp.ui.screens.trackdetails

import androidx.lifecycle.viewModelScope
import com.musicapp.playback.BasePlaybackViewModel
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TrackDetailsUiState {
    object Loading : TrackDetailsUiState()
    data class Success(val track: TrackModel) : TrackDetailsUiState()
    data class Error(val message: String) : TrackDetailsUiState()
}

class TrackDetailsViewModel(
    mediaPlayerManager: MediaPlayerManager,
) : BasePlaybackViewModel(mediaPlayerManager) {

    private val _uiState = MutableStateFlow<TrackDetailsUiState>(TrackDetailsUiState.Loading)
    val uiState: StateFlow<TrackDetailsUiState> = _uiState.asStateFlow()

    fun loadTrackDetails(trackId: Long) {
        viewModelScope.launch {
            _uiState.value = TrackDetailsUiState.Loading
            try {
                val track = playbackUiState.value.playbackQueue.find { it.id == trackId }
                    ?: playbackUiState.value.currentTrack?.takeIf { it.id == trackId }

                if (track != null) {
                    _uiState.value = TrackDetailsUiState.Success(track)
                } else {
                    _uiState.value = TrackDetailsUiState.Error("Track not found: $trackId")
                }
            } catch (e: Exception) {
                _uiState.value = TrackDetailsUiState.Error("Failed to load track details: ${e.message}")
            }
        }
    }
}