package com.musicapp.ui.screens.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.database.User
import com.musicapp.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.net.toUri

data class ProfileUiState(
    val currentUser: User? = null,
    val showChangeUsernameDialog: Boolean = false,
    val newUsernameInput: String = "",
    val showProfilePictureOptions: Boolean = false,
    val currentProfilePictureUri: Uri? = null,
    val isDefaultProfilePicture: Boolean = true
)

class ProfileScreenViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUser: StateFlow<User?> = auth.currentUser?.uid?.let { userId ->
        userRepository.getUser(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } ?: run {
        MutableStateFlow(null)
    }
    private val _showChangeUsernameDialog = MutableStateFlow(false)
    private val _newUsernameInput = MutableStateFlow("")
    private val _showProfilePictureOptions = MutableStateFlow(false)

    private val _derivedProfilePictureUri: StateFlow<Uri?> = _currentUser.map { user ->
        user?.profilePictureUri?.toUri()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _derivedIsDefaultProfilePicture: StateFlow<Boolean> = _currentUser.map { user ->
        user?.profilePictureUri.isNullOrBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val uiState: StateFlow<ProfileUiState> = combine(
        listOf(
            _currentUser,
            _showChangeUsernameDialog,
            _newUsernameInput,
            _showProfilePictureOptions,
            _derivedProfilePictureUri,
            _derivedIsDefaultProfilePicture
        )
    ) { values ->

        val user = values[0] as User?
        val showUsernameDialog = values[1] as Boolean
        val newUsername = values[2] as String
        val showPhotoOptions = values[3] as Boolean
        val profileUri = values[4] as Uri?
        val isDefault = values[5] as Boolean

        ProfileUiState(
            currentUser = user,
            showChangeUsernameDialog = showUsernameDialog,
            newUsernameInput = newUsername,
            showProfilePictureOptions = showPhotoOptions,
            currentProfilePictureUri = profileUri,
            isDefaultProfilePicture = isDefault
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )



    fun onNewUsernameChanged(username: String) {
        _newUsernameInput.update { username }
    }

    fun showUsernameDialog() {
        _showChangeUsernameDialog.value = true
        _newUsernameInput.value = uiState.value.currentUser?.username ?: ""
    }

    fun dismissUsernameDialog() {
        _showChangeUsernameDialog.value = false
        _newUsernameInput.value = ""
    }

    fun updateUsername() {
        val userId = auth.currentUser?.uid
        val currentUsername = uiState.value.currentUser?.username
        val newName = uiState.value.newUsernameInput.trim()

        if (userId == null || newName.isBlank() || newName == currentUsername) {
            dismissUsernameDialog()
            return
        }

        viewModelScope.launch {
            userRepository.updateUsername(newName, userId)
        }
    }

    fun showProfilePictureOptions() {
        _showProfilePictureOptions.value = true
    }

    fun dismissProfilePictureOptions() {
        _showProfilePictureOptions.value = false
    }

    fun updateProfilePicture(uri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            dismissProfilePictureOptions()
            return
        }

        viewModelScope.launch {
            try {
                userRepository.updateProfilePicture(uri, userId)
                dismissProfilePictureOptions()
            } catch (e: Exception) {
                Log.e("ProfileScreenViewModel", "Error updating profile picture", e)
                dismissProfilePictureOptions()
            }
        }
    }

    fun removeProfilePicture() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            dismissProfilePictureOptions()
            return
        }

        viewModelScope.launch {
            try {
                userRepository.removeProfilePicture(userId)
                dismissProfilePictureOptions()
            } catch (e: Exception) {
                Log.e("ProfileScreenViewModel", "Error removing profile picture", e)
                dismissProfilePictureOptions()
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}