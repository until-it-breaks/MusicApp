package com.musicapp.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.models.Theme
import com.musicapp.data.repositories.SettingsRepository
import com.musicapp.data.repositories.UserRepository
import com.musicapp.ui.models.UserModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SettingsViewmodel"

class SettingsViewModel(
    auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
): ViewModel() {
    private val _userId = MutableStateFlow<String?>(auth.currentUser?.uid)

    @OptIn(ExperimentalCoroutinesApi::class)
    val user: StateFlow<UserModel?> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            userRepository.getUser(userId).map { it.toModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val allowExplicit: StateFlow<Boolean> = settingsRepository.allowExplicit.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = true
    )

    val theme: StateFlow<Theme> = settingsRepository.theme.stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(),
         initialValue = Theme.Default
    )

    fun setAllowExplicit(enabled: Boolean) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    settingsRepository.setExplicit(enabled)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            }
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    settingsRepository.setTheme(theme)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            }
        }
    }
}