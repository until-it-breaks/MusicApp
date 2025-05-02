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
import kotlinx.coroutines.launch

class LoginViewModel(private val auth: FirebaseAuth): ViewModel() {
    private val _loginState = MutableStateFlow<OperationState>(OperationState.Idle)
    val loginState: StateFlow<OperationState> = _loginState

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        viewModelScope.launch {
            _loginState.value = OperationState.Ongoing
            try {
                auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _loginState.value = OperationState.Success
                        } else {
                            val errorMessage = when(task.exception) {
                                is FirebaseAuthInvalidUserException -> "Account is disabled/deleted"
                                is FirebaseAuthInvalidCredentialsException -> "Wrong credentials."
                                is FirebaseNetworkException -> "Network error. Please check your internet connection"
                                else -> "An unexpected error occurred. Please try again."
                            }
                            _loginState.value = OperationState.Error(errorMessage)
                        }
                    }
            } catch (e: Exception) {
                _loginState.value = OperationState.Error("An unexpected error occurred")
            }
        }
    }
}