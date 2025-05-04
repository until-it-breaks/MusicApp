package com.musicapp.ui.screens.passwordrecovery

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

data class PasswordRecoveryState(
    val email: String = "",
    val errorMessageId: Int? = null,
    val isLoading: Boolean = false,
    val emailSent: Boolean = false
) {
    val canSubmit = email.isNotBlank()
}

class PasswordRecoveryViewModel(private val auth: FirebaseAuth): ViewModel() {

    private val _state = MutableStateFlow(PasswordRecoveryState())
    val state: StateFlow<PasswordRecoveryState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun sendPasswordResetEmail() {
        if (!state.value.canSubmit) return

        _state.update { it.copy(isLoading = true, emailSent = false, errorMessageId = null) }

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(state.value.email.trim()).await()
                _state.update { it.copy(emailSent = true) }
            } catch (e: FirebaseAuthInvalidUserException) {
                _state.update { it.copy(errorMessageId = R.string.no_user_found) }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _state.update { it.copy(errorMessageId = R.string.invalid_email) }
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