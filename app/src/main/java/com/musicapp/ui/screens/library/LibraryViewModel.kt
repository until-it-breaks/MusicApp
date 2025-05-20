package com.musicapp.ui.screens.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.data.repositories.TrackHistoryRepository
import com.musicapp.data.repositories.UserPlaylistRepository
import com.musicapp.ui.models.LikedTracksPlaylistModel
import com.musicapp.ui.models.TrackHistoryModel
import com.musicapp.ui.models.UserPlaylistModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class LibraryUiState(
    val likedTracksPlaylist: LikedTracksPlaylistModel? = null,
    val trackHistory: TrackHistoryModel? = null,
    val showAuthError: Boolean = false
)

class LibraryViewModel(
    private val userPlaylistRepository: UserPlaylistRepository,
    private val likedTracksRepository: LikedTracksRepository,
    private val trackHistoryRepository: TrackHistoryRepository,
    private val auth: FirebaseAuth
): ViewModel() {
    private val _userId = MutableStateFlow<String?>(auth.currentUser?.uid)
    private val _uiState = MutableStateFlow(LibraryUiState())

    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists: StateFlow<List<UserPlaylistModel>> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            userPlaylistRepository.getUserPlaylistsWithTracks(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    init {
        loadUserLikedAndHistory()
    }

    private fun loadUserLikedAndHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return
        } else {
            _userId.value = userId
        }
        viewModelScope.launch {
            try {
                val (trackHistory, likedTracksPlaylist) = withContext(Dispatchers.IO) {
                    val trackHistory = async { trackHistoryRepository.getTrackHistoryWithTracks(userId) }
                    val likedTracksPlaylist = async { likedTracksRepository.getLikedTracksWithTracks(userId)}
                    Pair(trackHistory.await(), likedTracksPlaylist.await())
                }
                _uiState.update {
                    it.copy(
                        trackHistory = trackHistory.first(),
                        likedTracksPlaylist = likedTracksPlaylist.first()
                    )
                }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error loading playlists: ${e.localizedMessage}", e)
            }
        }
    }

    fun createPlaylist(name: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return
        }
        viewModelScope.launch {
            try {
                val playlist = UserPlaylistModel(
                    name = name,
                    ownerId = userId,
                    id = UUID.randomUUID().toString(),
                    lastEditTime = System.currentTimeMillis()
                )
                withContext(Dispatchers.IO) {
                    userPlaylistRepository.upsertPlaylist(playlist)
                }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error creating playlist: ${e.localizedMessage}", e)
            }
        }
    }
}