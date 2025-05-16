package com.musicapp.ui.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.repositories.PlaylistsRepository
import com.musicapp.ui.models.LikedTracksPlaylistModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LikedTracksState(
    val playlist: LikedTracksPlaylistModel? = null,
    val playlistIsLoading: Boolean = false,
    val error: String? = null
)

class LikedTracksViewModel(private val auth: FirebaseAuth, private val playlistsRepository: PlaylistsRepository): ViewModel() {
    private val _state = MutableStateFlow(LikedTracksState())
    val state: StateFlow<LikedTracksState> = _state.asStateFlow()

    init {
        loadLikedTracks()
    }

    private fun loadLikedTracks() {
        viewModelScope.launch {
            _state.update { it.copy(playlistIsLoading = true, error = null) }
            try {
                val userId =  auth.currentUser?.uid
                val result = withContext(Dispatchers.IO) {
                    playlistsRepository.getLikedTracksWithTracks(userId!!)
                }
                _state.update { it.copy(playlist = result.toModel()) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage ?: "Unexpected error") }
            } finally {
                _state.update { it.copy(playlistIsLoading = false) }
            }
        }
    }
}