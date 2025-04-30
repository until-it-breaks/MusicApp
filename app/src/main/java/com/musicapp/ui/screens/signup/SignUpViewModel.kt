package com.musicapp.ui.screens.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class SignUpViewModel: ViewModel(), KoinComponent {
    private val firebaseAuth: FirebaseAuth by inject<FirebaseAuth>()
    private val firestore: FirebaseFirestore by inject<FirebaseFirestore>()

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    fun signUp(email: String, password: String, username: String) {
        _signUpState.update { SignUpState.Loading }
        viewModelScope.launch {
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                authResult.user?.uid?.let { userId ->
                    saveUserData(userId, username)
                    _signUpState.update { SignUpState.Success }
                } ?: run {
                    _signUpState.update { SignUpState.Error("Could not retrieve user ID.") }
                }
            } catch (e: Exception) {
                _signUpState.update { SignUpState.Error(e.localizedMessage ?: "Sign up failed") }
            }
        }
    }

    private suspend fun saveUserData(userId: String, username: String) {
        try {
            val userDocument = firestore.collection("users").document(userId)
            val userData = hashMapOf(
                "username" to username,
                "email" to firebaseAuth.currentUser?.email
            )
            userDocument.set(userData).await()
        } catch (e: Exception) {
            println("Error saving user data to Firestore: ${e.localizedMessage}")
        }
    }

    sealed class SignUpState {
        object Idle: SignUpState()
        object Loading: SignUpState()
        object Success:  SignUpState()
        data class Error(val errorMessage: String) : SignUpState()
    }
}
