package com.musicapp.ui.screens.playlist

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.musicapp.data.repositories.UserPlaylistRepository
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.data.models.UserPlaylistModel
import com.musicapp.data.models.toModel
import com.musicapp.playback.BasePlaybackViewModel
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

private const val TAG = "PersonalPlaylistViewModel"

data class PersonalPlaylistState(
    val deletionSuccessful: Boolean = false,
    val isEditingName: Boolean = false,
    val newName: String = ""
) {
    val canSubmitNameChange = newName.isNotBlank()
}

@UnstableApi
class PersonalPlaylistViewModel(
    private val userPlaylistRepository: UserPlaylistRepository,
    mediaPlayerManager: MediaPlayerManager
) : BasePlaybackViewModel(mediaPlayerManager) {
    private val _selectedPlaylistId = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(PersonalPlaylistState())
    val uiState: StateFlow<PersonalPlaylistState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlist: StateFlow<UserPlaylistModel?> = _selectedPlaylistId
        .filterNotNull()
        .flatMapLatest { playlistId ->
            userPlaylistRepository.getPlaylistWithTracksAndArtists(playlistId).map { it?.toModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    fun loadPlaylistTracks(playlistId: String) {
        _selectedPlaylistId.value = playlistId
    }

    fun removeTrackFromPlaylist(trackId: Long) {
        viewModelScope.launch {
            _selectedPlaylistId.value?.let {
                try {
                    withContext(Dispatchers.IO) {
                        userPlaylistRepository.removeTrackFromPlaylist(it, trackId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage, e)
                }
            }
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            _selectedPlaylistId.value?.let {
                try {
                    withContext(Dispatchers.IO) {
                        userPlaylistRepository.deletePlaylist(it)
                    }
                    _uiState.update { it.copy(deletionSuccessful = true) }
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage, e)
                }
            }
        }
    }

    fun startEditingName(currentName: String) {
        _uiState.update { it.copy(isEditingName = true, newName = currentName) }
    }

    fun dismissEditingName() {
        _uiState.update { it.copy(isEditingName = false, newName = "") }
    }

    fun onPlaylistNameChanged(name: String) {
        _uiState.update { it.copy(newName = name) }
    }

    fun confirmNameChange() {
        val playlistId = _selectedPlaylistId.value ?: return
        val name = _uiState.value.newName
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userPlaylistRepository.editPlaylistName(playlistId, name)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                dismissEditingName()
            }
        }
    }
}