package com.musicapp.ui.screens.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.musicapp.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SignUpState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val errorMessageId: Int? = null,
    val isLoading: Boolean = false,
    val navigateToMain: Boolean = false
) {
    val canSubmit = username.isNotBlank() && email.isNotBlank() && password.isNotBlank()
}

class SignUpViewModel(private val auth: FirebaseAuth, private val store: FirebaseFirestore): ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    fun onUsernameChanged(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun signUp() {
        if (!state.value.canSubmit) return

        _state.update { it.copy(isLoading = true, errorMessageId = null) }

        viewModelScope.launch {
            try {
                auth.signOut()
                val authResult = auth.createUserWithEmailAndPassword(state.value.email.trim(), state.value.password.trim()).await()
                val userId = authResult.user?.uid
                if (userId != null) {
                    saveUserData(userId, state.value.username)
                    _state.update { it.copy(navigateToMain = true) }
                } else {
                    _state.update { it.copy(errorMessageId = R.string.unexpected_error) }
                }
            } catch (e: FirebaseNetworkException) {
                _state.update { it.copy(errorMessageId = R.string.network_error) }
            } catch (e: FirebaseAuthWeakPasswordException) {
                _state.update { it.copy(errorMessageId = R.string.weak_password) }
            } catch (e: FirebaseAuthUserCollisionException) {
                _state.update { it.copy(errorMessageId = R.string.email_already_in_use) }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _state.update { it.copy(errorMessageId = R.string.malformed_email) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessageId = R.string.unexpected_error) }
                auth.currentUser?.delete()
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun saveUserData(userId: String, username: String) {
        try {
            val userDocument = store.collection("users").document(userId)
            val userData = hashMapOf(
                "username" to username,
                "email" to auth.currentUser?.email
            )
            userDocument.set(userData).await()
        } catch (e: Exception) {
            Log.e("SIGNUP", e.localizedMessage ?: "Unexpected error while trying to save username")
            throw e
        }
    }
}