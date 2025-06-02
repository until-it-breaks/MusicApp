package com.musicapp.ui.screens.trackdetails

import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.musicapp.playback.BasePlaybackViewModel
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.playback.PlaybackUiState
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@UnstableApi
class TrackDetailsViewModel(
    mediaPlayerManager: MediaPlayerManager,
) : BasePlaybackViewModel(mediaPlayerManager) {

}