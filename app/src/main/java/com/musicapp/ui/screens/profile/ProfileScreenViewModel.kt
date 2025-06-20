package com.musicapp.ui.screens.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.repositories.UserRepository
import com.musicapp.data.models.UserModel
import com.musicapp.data.models.toModel
import com.musicapp.auth.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ProfileScreenViewModel"

data class ProfileUiState(
    val showChangeUsernameDialog: Boolean = false,
    val showProfilePictureOptions: Boolean = false,
    val showConfirmDelete: Boolean = false,
    val showConfirmLogout: Boolean = false,
    val newUsernameInput: String = "",
    val navigateToLogin: Boolean = false
) {
    val canChangeName = newUsernameInput.isNotBlank()
}

sealed class ProfileUiEvent {
    object LaunchCamera : ProfileUiEvent()
    object RequestCameraPermission : ProfileUiEvent()
}

class ProfileScreenViewModel(
    private val authManager: AuthManager,
    private val userRepository: UserRepository,
    private val appContext: Context
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: StateFlow<UserModel?> = authManager.userId
        .filterNotNull()
        .flatMapLatest {
            userRepository.getUser(it).map { it.toModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    override fun onCleared() {
        super.onCleared()
        authManager.cleanup()
    }

    fun onNewUsernameChanged(username: String) {
        _uiState.update { it.copy(newUsernameInput = username) }
    }

    fun showUsernameDialog() {
        _uiState.update { it.copy(showChangeUsernameDialog = true, newUsernameInput = currentUser.value?.username ?: "Unknown") }
    }

    fun dismissUsernameDialog() {
        _uiState.update { it.copy(showChangeUsernameDialog = false) }
    }

    fun showProfilePictureOptions() {
        _uiState.update { it.copy(showProfilePictureOptions = true) }
    }

    fun dismissProfilePictureOptions() {
        _uiState.update { it.copy(showProfilePictureOptions = false) }
    }

    fun showConfirmDelete() {
        _uiState.update { it.copy(showConfirmDelete = true) }
    }

    fun dismissConfirmDelete() {
        _uiState.update { it.copy(showConfirmDelete = false) }
    }

    fun showConfirmLogout() {
        _uiState.update { it.copy(showConfirmLogout = true) }
    }

    fun dismissConfirmLogout() {
        _uiState.update { it.copy(showConfirmLogout = false) }
    }

    fun updateUsername() {
        val userId = authManager.userId.value
        val currentUsername = currentUser.value?.username
        val newName = uiState.value.newUsernameInput.trim()

        if (userId == null || newName.isBlank() || newName == currentUsername) {
            dismissUsernameDialog()
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userRepository.updateUsername(newName, userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                dismissUsernameDialog()
            }
        }
    }

    fun updateProfilePicture(uri: Uri) {
        val userId = authManager.userId.value
        if (userId == null) {
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userRepository.updateProfilePicture(uri, userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                dismissProfilePictureOptions()
            }
        }
    }

    fun removeProfilePicture() {
        val userId = authManager.userId.value
        if (userId == null) {
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userRepository.removeProfilePicture(userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                dismissProfilePictureOptions()
            }
        }
    }

    fun onTakePhotoClicked() {
        dismissProfilePictureOptions()
        if (authManager.userId.value == null) {
            return
        }
        if (appContext.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            viewModelScope.launch {
                _events.send(ProfileUiEvent.LaunchCamera)
            }
        } else {
            viewModelScope.launch {
                _events.send(ProfileUiEvent.RequestCameraPermission)
            }
        }
    }

    fun onCameraPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            viewModelScope.launch {
                _events.send(ProfileUiEvent.LaunchCamera)
            }
        } else {
            Log.d(TAG, "Camera permission denied.")
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            currentUser.value?.userId?.let {
                withContext(Dispatchers.IO) {
                    userRepository.deleteUser(it)
                }
            }
            authManager.deleteAccount()
            _uiState.update { it.copy(navigateToLogin = true, showConfirmDelete = false) }
        }
    }

    fun logout() {
        authManager.logout()
        _uiState.update { it.copy(navigateToLogin = true, showConfirmLogout = false) }
    }
}