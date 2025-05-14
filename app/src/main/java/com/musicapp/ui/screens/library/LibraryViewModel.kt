package com.musicapp.ui.screens.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.database.LikedTracksPlaylist
import com.musicapp.data.database.Playlist
import com.musicapp.data.database.TrackHistory
import com.musicapp.data.repositories.PlaylistsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryState(
    val isLoading: Boolean = false,
    val likedTracksPlaylist: LikedTracksPlaylist? = null,
    val trackHistory: TrackHistory? = null,
    val playlists: Flow<List<Playlist>> = MutableStateFlow(emptyList()),
)

class LibraryViewModel(private val playlistsRepository: PlaylistsRepository, private val auth: FirebaseAuth): ViewModel() {
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val userId = auth.currentUser!!.uid
                _state.update {
                    it.copy(
                        trackHistory = playlistsRepository.getTrackHistory(userId),
                        likedTracksPlaylist = playlistsRepository.getLikedTracks(userId),
                        playlists = playlistsRepository.getUserPlaylists(userId)
                    )
                }
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error loading playlists: ${e.localizedMessage}")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val playlist = Playlist(name = name, ownerId = auth.currentUser!!.uid)
            playlistsRepository.upsertPlaylist(playlist)
        }
    }
}