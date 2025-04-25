package com.musicapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _loginState.value = LoginState.Success
                        } else {
                            _loginState.value = LoginState.Error(task.exception?.message ?: "Login failed")
                        }
                    }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }
    }

    sealed class LoginState {
        object Idle: LoginState()
        object Loading: LoginState()
        object Success:  LoginState()
        data class Error(val errorMessage: String) : LoginState()
    }
}