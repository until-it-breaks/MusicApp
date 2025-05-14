package com.musicapp.ui.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.deezer.DeezerPlaylistDetailed
import com.musicapp.data.remote.deezer.DeezerTrackDetailed
import com.musicapp.data.repositories.PlaylistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class UserPlaylistState(
    val playlistId: String? = null,
    val playlistDetails: DeezerPlaylistDetailed? = null,
    val tracks: List<DeezerTrackDetailed> = emptyList(),
    val playlistDetailsAreLoading: Boolean = false,
    val tracksAreLoading: Boolean = false,
    val error: String? = null
)

class UserPlaylistViewModel(): ViewModel(), KoinComponent {
    private val _state = MutableStateFlow(UserPlaylistState())
    val state: StateFlow<UserPlaylistState> = _state.asStateFlow()
    val playlistRepository: PlaylistsRepository by inject()

    fun loadPlaylistTracks(playlistId: String) {
        _state.update { it.copy(playlistId = playlistId) }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            val playlistId = state.value.playlistId
            if (playlistId != null) {
                playlistRepository.deletePlaylistById(playlistId)
            }
        }
    }
}