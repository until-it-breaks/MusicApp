package com.musicapp.ui.screens.main.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.DeezerArtist
import com.musicapp.data.remote.DeezerDataSource
import com.musicapp.data.remote.DeezerPlaylist
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class HomeState(
    val refreshing: Boolean = false,
    val playlists: List<DeezerPlaylist> = emptyList(),
    val artists: List<DeezerArtist> = emptyList()
)

class HomeViewModel: ViewModel(), KoinComponent {
    private val deezerDataSource: DeezerDataSource by inject()
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        _state.update { it.copy(refreshing = true) }
        loadTopPlaylist()
        loadTopArtists()
        _state.update { it.copy(refreshing = false) }
    }

    private fun loadTopPlaylist() {
        viewModelScope.launch {
            try {
                val result = deezerDataSource.getTopPlaylists()
                _state.update { it.copy(playlists = result.shuffled()) }
            } catch (e: Exception) {
                Log.e(null, e.localizedMessage ?: "Unexpected error")
            }
        }
    }

    private fun loadTopArtists() {
        viewModelScope.launch {
            try {
                val result = deezerDataSource.getTopArtists()
                _state.update { it.copy(artists = result.shuffled()) }
            } catch (e: Exception) {
                Log.e(null, e.localizedMessage ?: "Unexpected error")
            }
        }
    }

    fun reloadContent() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true, playlists = emptyList(), artists = emptyList()) }
           delay(500)
            loadTopPlaylist()
            loadTopArtists()
            _state.update { it.copy(refreshing = false) }
        }
    }
}