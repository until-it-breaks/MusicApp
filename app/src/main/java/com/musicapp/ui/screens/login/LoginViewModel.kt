package com.musicapp.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.musicapp.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "LoginViewModel"

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val errorMessageId: Int? = null,
    val isLoading: Boolean = false,
    val navigateToMain: Boolean = false
) {
    val canSubmit = email.isNotBlank() && password.isNotBlank()
}

class LoginViewModel(private val auth: FirebaseAuth): ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password)}
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun resetUiState() {
        _uiState.value = LoginState()
    }

    fun login() {
        if (!uiState.value.canSubmit) return

        _uiState.update { it.copy(isLoading = true, errorMessageId = null) }

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(uiState.value.email.trim(), uiState.value.password.trim()).await()
                _uiState.update { it.copy(navigateToMain = true) }
            } catch (e: FirebaseAuthInvalidUserException) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.account_not_found_or_disabled) }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(errorMessageId = R.string.invalid_credentials) }
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