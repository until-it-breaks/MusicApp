package com.musicapp.ui.screens.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.musicapp.R
import com.musicapp.data.repositories.UserRepository
import com.musicapp.ui.models.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "SignUpViewModel"

data class SignUpState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val errorMessageId: Int? = null,
    val isLoading: Boolean = false,
    val navigateToMain: Boolean = false
) {
    val canSubmit = username.isNotBlank() && email.isNotBlank() && password.isNotBlank()
}

class SignUpViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun signUp() {
        if (!uiState.value.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageId = null) }
            try {
                auth.signOut()
                val authResult = auth.createUserWithEmailAndPassword(uiState.value.email.trim(), uiState.value.password.trim()).await()

                val userId = authResult.user?.uid
                val username = _uiState.value.username
                val email = authResult.user?.email

                if (userId != null && email != null) {
                    createLocalUser(userId, username, email)
                    _uiState.update { it.copy(navigateToMain = true) }
                } else {
                    _uiState.update { it.copy(errorMessageId = R.string.unexpected_error) }
                }
            } catch (e: FirebaseNetworkException) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.network_error) }
            } catch (e: FirebaseAuthWeakPasswordException) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.weak_password) }
            } catch (e: FirebaseAuthUserCollisionException) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.email_already_in_use) }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.malformed_email) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.unexpected_error) }
                auth.currentUser?.delete()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun createLocalUser(userId: String, username: String, email: String) {
        val user = UserModel(userId, username, email)
        userRepository.createNewUser(user)
    }
}