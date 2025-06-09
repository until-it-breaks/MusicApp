package com.musicapp.ui.screens.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.repositories.UserPlaylistRepository
import com.musicapp.ui.models.UserPlaylistModel
import com.musicapp.ui.models.toModel
import com.musicapp.ui.screens.AuthManager
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
import java.util.UUID

private const val TAG = "LibraryViewModel"

data class LibraryUiState(
    val showAuthError: Boolean = false
)

class LibraryViewModel(
    private val userPlaylistRepository: UserPlaylistRepository,
    private val authManager: AuthManager
): ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists: StateFlow<List<UserPlaylistModel>> = authManager.userId
        .filterNotNull()
        .flatMapLatest { userId ->
            userPlaylistRepository.getPlaylists(userId).map { it.map { it.toModel() } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    override fun onCleared() {
        super.onCleared()
        authManager.cleanup()
    }

    fun createPlaylist(name: String) {
        val userId = authManager.userId.value
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
                    userPlaylistRepository.insertPlaylist(playlist)
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