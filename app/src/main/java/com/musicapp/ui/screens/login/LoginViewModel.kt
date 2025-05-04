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
import kotlinx.coroutines.tasks.await

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
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password)}
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun resetState() {
        _state.value = LoginState()
    }

    fun login() {
        if (!state.value.canSubmit) return

        _state.update { it.copy(isLoading = true, errorMessageId = null) }

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(state.value.email.trim(), state.value.password.trim()).await()
                _state.update { it.copy(navigateToMain = true) }
            } catch (e: FirebaseAuthInvalidUserException) {
                _state.update { it.copy(errorMessageId = R.string.account_not_found_or_disabled) }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _state.update { it.copy(errorMessageId = R.string.invalid_credentials) }
            } catch (e: FirebaseNetworkException) {
                _state.update { it.copy(errorMessageId = R.string.network_error) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessageId = R.string.unexpected_error) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}