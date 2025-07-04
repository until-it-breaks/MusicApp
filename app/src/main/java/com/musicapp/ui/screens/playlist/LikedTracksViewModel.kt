package com.musicapp.ui.screens.playlist

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.playback.BasePlaybackViewModel
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.data.models.LikedTracksPlaylistModel
import com.musicapp.data.models.toModel
import com.musicapp.auth.AuthManager
import com.musicapp.data.models.TrackModel
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

private const val TAG = "LikedTracksViewModel"

data class LikedTracksState(val showAuthError: Boolean = false)

@UnstableApi
class LikedTracksViewModel(
    private val likedTracksRepository: LikedTracksRepository,
    private val authManager: AuthManager,
    mediaPlayerManager: MediaPlayerManager
): BasePlaybackViewModel(mediaPlayerManager) {
    private val _uiState = MutableStateFlow(LikedTracksState())
    val uiState: StateFlow<LikedTracksState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlist: StateFlow<LikedTracksPlaylistModel?> = authManager.userId
        .filterNotNull()
        .flatMapLatest { userId ->
            likedTracksRepository.getPlaylistWithTracksAndArtists(userId).map { it.toModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    override fun onCleared() {
        super.onCleared()
        authManager.cleanup()
    }

    fun clearLikedTracks() {
        val userId = authManager.userId.value
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    likedTracksRepository.clearLikedTracks(userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            }
        }
    }

    fun removeTrackFromLikedTracks(track: TrackModel) {
        val userId = authManager.userId.value
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    likedTracksRepository.removeTrackFromLikedTracks(userId, track)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            }
        }
    }

    fun logout() {
        authManager.logout()
    }
}