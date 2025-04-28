package com.musicapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadUsername()
    }

    private fun loadUsername() {
        viewModelScope.launch {
            _loading.value = true
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                try {
                    val userDocument = firestore.collection("users").document(userId).get().await()
                    if (userDocument.exists()) {
                        _username.value = userDocument.getString("username")
                        _loading.value = false
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "User data not found."
                        _loading.value = false
                    }
                } catch (e: Exception) {
                    _errorMessage.value = e.localizedMessage ?: "Failed to load username"
                    _loading.value = false
                }
            } else {
                _errorMessage.value = "User not authenticated."
                _loading.value = false
            }
        }
    }
}