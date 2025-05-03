package com.musicapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.musicapp.data.util.OperationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.musicapp.R

class PasswordRecoveryViewModel(private val auth: FirebaseAuth): ViewModel() {
    private val _recoveryState = MutableStateFlow<OperationState>(OperationState.Idle)
    val recoveryState: StateFlow<OperationState> = _recoveryState.asStateFlow()

    fun sendPasswordResetEmail(email: String) {
        val trimmedEmail = email.trim()

        viewModelScope.launch {
            _recoveryState.value = OperationState.Ongoing
            auth.sendPasswordResetEmail(trimmedEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _recoveryState.value = OperationState.Success
                } else {
                    val errorKey = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> R.string.no_user_found
                        is FirebaseAuthInvalidCredentialsException -> R.string.invalid_email
                        is FirebaseNetworkException -> R.string.network_error
                        else -> R.string.unexpected_error
                    }
                    _recoveryState.value = OperationState.Error(stringKey = errorKey)
                }
            }
        }
    }

    fun resetState() {
        _recoveryState.value = OperationState.Idle
    }
}