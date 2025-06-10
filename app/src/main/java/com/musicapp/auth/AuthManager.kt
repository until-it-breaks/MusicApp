package com.musicapp.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(private val auth: FirebaseAuth) {
    private val _userId = MutableStateFlow<String?>(auth.currentUser?.uid)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener {
        _userId.value = it.currentUser?.uid
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun logout() {
        auth.signOut()
    }

    fun deleteAccount() {
        auth.currentUser?.delete()
    }

    fun cleanup() {
        auth.removeAuthStateListener(authListener)
    }
}