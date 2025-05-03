package com.musicapp

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.ui.MusicAppRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(auth: FirebaseAuth): ViewModel() {
    //TODO might need stricter check
    private val _initialRoute = MutableStateFlow(if (auth.currentUser != null) MusicAppRoute.Main else MusicAppRoute.Login)
    val initialRoute = _initialRoute.asStateFlow()
}