package com.musicapp.ui.screens.login

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

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val errorMessageId: Int? = null,
    val isLoading: Boolean = false,
    val navigateToMain: Boolean = false
) {
    val canSubmit = email.isNotBlank() && password.isNotBlank() // Can be improved
}

class LoginViewModel(private val auth: FirebaseAuth): ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password)}
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !state.value.isPasswordVisible) }
    }

    fun login() {
        if (!state.value.canSubmit) return

        _state.update { it.copy(isLoading = true, errorMessageId = null) }

        viewModelScope.launch {
            auth.signInWithEmailAndPassword(state.value.email, state.value.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _state.update { it.copy(isLoading = false, navigateToMain = true) }
                    } else {
                        val errorKey = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> R.string.account_not_found_or_disabled
                            is FirebaseAuthInvalidCredentialsException -> R.string.invalid_credentials
                            is FirebaseNetworkException -> R.string.network_error
                            else -> R.string.unexpected_error
                        }
                        _state.update { it.copy(isLoading = false, errorMessageId = errorKey) }
                    }
            }
        }
    }

    fun resetNavigation() {
        _state.update { it.copy(navigateToMain = false) }
    }
}