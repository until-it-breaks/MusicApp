package com.musicapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.musicapp.R
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
                    val errorKey = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> R.string.account_not_found_or_disabled
                        is FirebaseAuthInvalidCredentialsException -> R.string.invalid_credentials
                        is FirebaseNetworkException -> R.string.network_error
                        else -> R.string.unexpected_error
                    }
                    _loginState.value = OperationState.Error(stringKey = errorKey)
                }
            }
        }
    }

    fun resetState() {
        _loginState.value = OperationState.Idle
    }
}