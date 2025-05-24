package com.musicapp.ui.screens.password

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.musicapp.R
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

private const val TAG = "PasswordRecoveryViewModel"

data class PasswordRecoveryState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailSent: Boolean = false,
    val errorMessageId: Int? = null
) {
    val canSubmit = email.isNotBlank()
}

class PasswordRecoveryViewModel(private val auth: FirebaseAuth): ViewModel() {
    private val _uiState = MutableStateFlow(PasswordRecoveryState())
    val uiState: StateFlow<PasswordRecoveryState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun sendPasswordResetEmail() {
        if (!uiState.value.canSubmit) return

        _uiState.update { it.copy(isLoading = true, emailSent = false, errorMessageId = null) }

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(uiState.value.email.trim()).await()
                _uiState.update { it.copy(emailSent = true, email = "") }
            } catch (e: FirebaseAuthInvalidUserException) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.no_user_found) }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.invalid_email) }
            } catch (e: FirebaseNetworkException) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.network_error) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.unexpected_error) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}