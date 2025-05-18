package com.musicapp.ui.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.remote.deezer.DeezerPlaylistDetailed
import com.musicapp.data.repositories.PlaylistsRepository
import com.musicapp.ui.models.LikedTracksPlaylistModel
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class PersonalPlaylistState(
    val playlistId: String? = null,
    val playlistDetails: DeezerPlaylistDetailed? = null,
    val tracks: List<TrackModel> = emptyList(),
    val playlistDetailsAreLoading: Boolean = false,
    val tracksAreLoading: Boolean = false,
    val error: String? = null
)

class PersonalPlaylistViewModel(private val auth: FirebaseAuth, private val playlistsRepository: PlaylistsRepository): ViewModel() {
    private val _userId = MutableStateFlow(auth.currentUser?.uid)
    private val _uiState = MutableStateFlow(LikedTracksState())
    val uiState: StateFlow<LikedTracksState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlist: StateFlow<LikedTracksPlaylistModel?> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            playlistsRepository.getLikedTracksWithTracks(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    /*
    fun loadPlaylistTracks(playlistId: String) {
        _state.update { it.copy(playlistId = playlistId) }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            val playlistId = state.value.playlistId
            if (playlistId != null) {
                playlistRepository.deletePlaylist(playlistId)
            }
        }
    }
    */
}