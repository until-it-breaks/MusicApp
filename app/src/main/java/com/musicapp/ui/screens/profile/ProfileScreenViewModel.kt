package com.musicapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileScreenViewModel(private val auth: FirebaseAuth): ViewModel() {
    fun logout() {
        auth.signOut()
    }
}