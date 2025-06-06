package com.musicapp.ui.screens.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.database.User
import com.musicapp.data.repositories.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProfileUiState(
    val currentUser: User? = null,
    val showChangeUsernameDialog: Boolean = false,
    val newUsernameInput: String = "",
    val showProfilePictureOptions: Boolean = false,
    val currentProfilePictureUri: Uri? = null,
    val isDefaultProfilePicture: Boolean = true
)

sealed class ProfileUiEvent {
    data class LaunchCamera(val uri: Uri) : ProfileUiEvent()
    object RequestCameraPermission : ProfileUiEvent()
}

class ProfileScreenViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val appContext: Context
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

    private val _events = Channel<ProfileUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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
            } catch (e: Exception) {
                Log.e("ProfileScreenViewModel", "Error updating profile picture", e)
                Toast.makeText(appContext, "Error updating profile picture", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(appContext, "Error removing profile picture", Toast.LENGTH_SHORT).show()
                dismissProfilePictureOptions()
            }
        }
    }

    fun onTakePhotoClicked() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            dismissProfilePictureOptions()
            return
        }

        if (appContext.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // have permissions
            val tempUri = createTempImageUri()
            if (tempUri != null) {
                viewModelScope.launch {
                    _events.send(ProfileUiEvent.LaunchCamera(tempUri))
                }
            } else {
                Log.e("ProfileScreenViewModel", "Failed to create temporary URI for camera.")
                dismissProfilePictureOptions()
            }
        } else {
            // no permissions, request
            viewModelScope.launch {
                _events.send(ProfileUiEvent.RequestCameraPermission)
            }
        }
    }

    fun onCameraPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            val tempUri = createTempImageUri()
            if (tempUri != null) {
                viewModelScope.launch {
                    _events.send(ProfileUiEvent.LaunchCamera(tempUri))
                }
            } else {
                Log.e("ProfileScreenViewModel", "Failed to create temporary URI after permission granted.")
                dismissProfilePictureOptions()
            }
        } else {
            Log.d("ProfileScreenViewModel", "Camera permission denied.")
            dismissProfilePictureOptions()
            Toast.makeText(appContext, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun createTempImageUri(): Uri? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timestamp}_.jpg"

            val storageDir = appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (storageDir == null) {
                Log.e("ProfileScreenViewModel", "External storage directory not available.")
                return null
            }
            val photoFile = File(storageDir, imageFileName)
            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                photoFile
            )
        } catch (e: Exception) {
            Log.e("ProfileScreenViewModel", "Error creating temporary image file URI: ${e.message}", e)
            null
        }
    }
}