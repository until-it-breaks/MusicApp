package com.musicapp.ui.screens.playlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.repositories.TrackHistoryRepository
import com.musicapp.ui.models.TrackHistoryModel
import com.musicapp.ui.models.TrackModel
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

data class TrackHistoryState(val showAuthError: Boolean = false)

class TrackHistoryViewModel(
    private val auth: FirebaseAuth,
    private val playlistsRepository: TrackHistoryRepository
): ViewModel() {
    private val _userId = MutableStateFlow(auth.currentUser?.uid)
    private val _uiState = MutableStateFlow(TrackHistoryState())
    val uiState: StateFlow<TrackHistoryState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlist: StateFlow<TrackHistoryModel?> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            playlistsRepository.getTrackHistoryWithTracks(userId).map { it.toModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    fun clearTrackHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return
        } else {
            _userId.value = userId
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    playlistsRepository.clearTrackHistory(userId)
                }
            } catch (e: Exception) {
                Log.e("TrackHistoryViewModel", "Error clearing track history: ${e.localizedMessage}", e)
            }
        }
    }

    fun removeTrackFromTrackHistory(trackId: Long) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(showAuthError = true) }
            return
        } else {
            _userId.value = userId
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    playlistsRepository.removeTrackFromTrackHistory(userId, trackId)
                }
            } catch (e: Exception) {
                Log.e("TrackHistoryViewModel", "Error removing track from track history: ${e.localizedMessage}", e)
            }
        }
    }

    fun addToQueue(track: TrackModel) {
        viewModelScope.launch {
            // TODO Enqueue given track
        }
    }

    fun playTrack(track: TrackModel) {
        viewModelScope.launch {
            // TODO Play given track
        }
    }
}