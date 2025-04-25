package com.musicapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    fun signUp(email: String, password: String, username: String) {
        _signUpState.value = SignUpState.Loading
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _signUpState.value = SignUpState.Success
                        } else {
                            _signUpState.value = SignUpState.Error(task.exception?.message ?: "Sign up failed")
                        }
                    }
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }
    }

    sealed class SignUpState {
        object Idle: SignUpState()
        object Loading: SignUpState()
        object Success:  SignUpState()
        data class Error(val errorMessage: String) : SignUpState()
    }
}
