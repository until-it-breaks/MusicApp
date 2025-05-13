package com.musicapp.ui.screens.main.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.database.LikedTracksPlaylist
import com.musicapp.data.database.Playlist
import com.musicapp.data.database.TrackHistory
import com.musicapp.data.repositories.PlaylistsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

data class LibraryState(
    val isLoading: Boolean = false,
    val likedTracksPlaylist: LikedTracksPlaylist? = null,
    val trackHistory: TrackHistory? = null,
    val playlists: List<Playlist> = emptyList(),
)

class LibraryViewModel(): ViewModel(), KoinComponent {
    private val playlistRepository: PlaylistsRepository by inject()
    private val auth: FirebaseAuth by inject()

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
                        trackHistory = playlistRepository.getTrackHistory(userId),
                        likedTracksPlaylist = playlistRepository.getLikedTracks(userId),
                        playlists = playlistRepository.getUserPlaylists(userId)
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
            playlistRepository.upsertPlaylist(playlist)
        }
    }
}