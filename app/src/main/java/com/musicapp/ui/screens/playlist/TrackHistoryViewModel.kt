package com.musicapp.ui.screens.playlist

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.musicapp.data.repositories.TrackHistoryRepository
import com.musicapp.playback.BasePlaybackViewModel
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.data.models.TrackHistoryModel
import com.musicapp.data.models.toModel
import com.musicapp.auth.AuthManager
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

private const val TAG = "TrackHistoryViewModel"

data class TrackHistoryState(val showAuthError: Boolean = false)

@UnstableApi
class TrackHistoryViewModel(
    private val authManager: AuthManager,
    private val trackHistoryRepository: TrackHistoryRepository,
    mediaPlayerManager: MediaPlayerManager
): BasePlaybackViewModel(mediaPlayerManager) {
    private val _uiState = MutableStateFlow(TrackHistoryState())
    val uiState: StateFlow<TrackHistoryState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlist: StateFlow<TrackHistoryModel?> = authManager.userId
        .filterNotNull()
        .flatMapLatest { userId ->
            trackHistoryRepository.getTrackHistoryWithTracksAndArtists(userId).map { it.toModel() }
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

    fun clearTrackHistory() {
        val userId = authManager.userId.value
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    trackHistoryRepository.clearTrackHistory(userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            }
        }
    }

    fun removeTrackFromTrackHistory(trackId: Long) {
        val userId = authManager.userId.value
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    trackHistoryRepository.removeTrackFromTrackHistory(userId, trackId)
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