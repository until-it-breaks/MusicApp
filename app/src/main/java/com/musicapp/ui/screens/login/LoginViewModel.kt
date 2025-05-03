package com.musicapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.util.OperationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val auth: FirebaseAuth): ViewModel() {
    private val _loginState = MutableStateFlow<OperationState>(OperationState.Idle)
    val loginState: StateFlow<OperationState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        viewModelScope.launch {
            _loginState.value = OperationState.Ongoing
            auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginState.value = OperationState.Success
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "An unexpected error occurred"
                    _loginState.value = OperationState.Error(errorMessage)
                }
            }
        }
    }
}