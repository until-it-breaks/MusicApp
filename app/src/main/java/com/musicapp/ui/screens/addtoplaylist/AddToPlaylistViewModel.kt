package com.musicapp.ui.screens.addtoplaylist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.repositories.PlaylistsRepository
import com.musicapp.ui.models.LikedTracksPlaylistModel
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.UserPlaylistModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AddToPlaylistUiState(
    val likedTracksPlaylist: LikedTracksPlaylistModel? = null,
    val playlists: List<UserPlaylistModel> = emptyList(),
    val showAuthError: Boolean = false
)

class AddToPlaylistViewModel(
    private val playlistsRepository: PlaylistsRepository,
    private val auth: FirebaseAuth
): ViewModel() {
    private val _uiState = MutableStateFlow(AddToPlaylistUiState())
    val uiState: StateFlow<AddToPlaylistUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return
        }
        viewModelScope.launch {
            try {
                val (likedTracksPlaylist, playlists) = withContext(Dispatchers.IO) {
                    val likedTracksPlaylist = async { playlistsRepository.getLikedTracksWithTracks(userId)}
                    val playlists = async { playlistsRepository.getUserPlaylistsWithTracks(userId) }
                    Pair(likedTracksPlaylist.await(), playlists.await())
                }
                _uiState.update {
                    it.copy(
                        likedTracksPlaylist = likedTracksPlaylist.first(),
                        playlists = playlists.first()
                    )
                }
            } catch (e: Exception) {
                Log.e("AddToPlaylistViewModel", "Error loading playlists: ${e.localizedMessage}", e)
            }
        }
    }

    fun addToLiked(track: TrackModel) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.update { it.copy(showAuthError = true) }
            } else {
                withContext(Dispatchers.IO) {
                    playlistsRepository.addTrackToLikedTracksPlaylist(userId, track)
                }
            }
        }
    }

    suspend fun isInLiked(track: TrackModel): Boolean {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return true
        } else {
            return playlistsRepository.isTrackInLikedTracks(userId, track)
        }
    }

    suspend fun isInPlaylist(playlistId: String, track: TrackModel): Boolean {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return true
        } else {
            return playlistsRepository.isTrackInPlaylist(playlistId, track)
        }
    }

    fun addToPlaylists(track: TrackModel, playlistIds: Set<String> ) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.update { it.copy(showAuthError = true) }
            } else {
                for (playlistId in playlistIds) {
                    withContext(Dispatchers.IO) {
                        playlistsRepository.addTrackToPlaylist(playlistId, track)
                    }
                }
            }
        }
    }
}