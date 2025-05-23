package com.musicapp.ui.screens.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.repositories.UserPlaylistRepository
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
import java.util.UUID

private const val TAG = "LibraryViewModel"

data class LibraryUiState(
    val showAuthError: Boolean = false
)

class LibraryViewModel(private val userPlaylistRepository: UserPlaylistRepository, private val auth: FirebaseAuth): ViewModel() {
    private val _userId = MutableStateFlow<String?>(auth.currentUser?.uid)
    private val _uiState = MutableStateFlow(LibraryUiState())

    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists: StateFlow<List<UserPlaylistModel>> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            userPlaylistRepository.getPlaylists(userId).map { it.map { it.toModel() } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

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
                    userPlaylistRepository.insertPlaylist(playlist)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}