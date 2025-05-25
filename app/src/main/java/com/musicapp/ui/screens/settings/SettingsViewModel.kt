package com.musicapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.models.Theme
import com.musicapp.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository): ViewModel() {

    val allowExplicit: StateFlow<Boolean> =
        repository.allowExplicit.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = true
        )

    val theme: StateFlow<Theme> = repository.theme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        Theme.Default
    )

    fun setAllowExplicit(enabled: Boolean) {
        viewModelScope.launch {
            repository.setExplicit(enabled)
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            repository.setTheme(theme)
        }
    }
}