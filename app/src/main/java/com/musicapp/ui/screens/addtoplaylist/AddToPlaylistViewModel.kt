package com.musicapp.ui.screens.addtoplaylist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.data.repositories.UserPlaylistRepository
import com.musicapp.ui.models.LikedTracksPlaylistModel
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.UserPlaylistModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "AddToPlaylistViewModel"

data class AddToPlaylistUiState(
    val showAuthError: Boolean = false
)

class AddToPlaylistViewModel(
    private val likedTracksRepository: LikedTracksRepository,
    private val userPlaylistRepository: UserPlaylistRepository,
    private val auth: FirebaseAuth
): ViewModel() {
    private val _userId = MutableStateFlow<String?>(auth.currentUser?.uid)

    private val _uiState = MutableStateFlow(AddToPlaylistUiState())
    val uiState: StateFlow<AddToPlaylistUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists: StateFlow<List<UserPlaylistModel>> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            userPlaylistRepository.getPlaylistsWithTracksFlow(userId).map { it.map { it.toModel() } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val likedPlaylist: StateFlow<LikedTracksPlaylistModel?> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            likedTracksRepository.getPlaylistWithTracks(userId).map { it.toModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    fun addToLiked(track: TrackModel) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.update { it.copy(showAuthError = true) }
            } else {
                try {
                    withContext(Dispatchers.IO) {
                        likedTracksRepository.addTrackToLikedTracks(userId, track)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage, e)
                }
            }
        }
    }

    fun addToPlaylists(track: TrackModel, playlistIds: Set<String> ) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _uiState.update { it.copy(showAuthError = true) }
            } else {
                for (playlistId in playlistIds) {
                    try {
                        withContext(Dispatchers.IO) {
                            userPlaylistRepository.addTrackToPlaylist(playlistId, track)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, e.localizedMessage, e)
                    }
                }
            }
        }
    }
}