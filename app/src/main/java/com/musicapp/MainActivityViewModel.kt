package com.musicapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.models.Theme
import com.musicapp.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MainActivityViewModel(
    private val auth: FirebaseAuth,
    settingsRepository: SettingsRepository
): ViewModel() {
    val theme = settingsRepository.theme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = Theme.Default
    )

    fun isSessionActive() = auth.currentUser != null
}