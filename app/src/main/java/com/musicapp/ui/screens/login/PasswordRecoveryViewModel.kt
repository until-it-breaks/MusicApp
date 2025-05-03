package com.musicapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.util.OperationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasswordRecoveryViewModel(private val auth: FirebaseAuth): ViewModel() {
    private val _recoveryProcessState = MutableStateFlow<OperationState>(OperationState.Idle)
    val recoveryProcessState: StateFlow<OperationState> = _recoveryProcessState.asStateFlow()

    fun sendPasswordResetEmail(email: String) {
        val trimmedEmail = email.trim()

        viewModelScope.launch {
            _recoveryProcessState.value = OperationState.Ongoing
            auth.sendPasswordResetEmail(trimmedEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _recoveryProcessState.value = OperationState.Success
                } else {
                    val message = task.exception?.localizedMessage ?: "An unexpected error occurred"
                    _recoveryProcessState.value = OperationState.Error(message)
                }
            }
        }
    }
}