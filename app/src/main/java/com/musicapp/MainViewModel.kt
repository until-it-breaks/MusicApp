package com.musicapp

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class MainViewModel(private val auth: FirebaseAuth): ViewModel() {
    fun isSessionActive() = auth.currentUser != null
}