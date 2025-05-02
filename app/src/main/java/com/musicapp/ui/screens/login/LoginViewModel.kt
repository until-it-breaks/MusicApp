package com.musicapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.util.OperationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val auth: FirebaseAuth): ViewModel() {
    private val _loginState = MutableStateFlow<OperationState>(OperationState.Idle)
    val loginState: StateFlow<OperationState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = OperationState.Ongoing
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _loginState.value = OperationState.Success
                        } else {
                            _loginState.value = OperationState.Error(task.exception?.message ?: "Login failed")
                        }
                    }
            } catch (e: Exception) {
                _loginState.value = OperationState.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }
    }
}