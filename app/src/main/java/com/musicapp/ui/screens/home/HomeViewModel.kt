package com.musicapp.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.deezer.DeezerChartAlbum
import com.musicapp.data.remote.deezer.DeezerArtist
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerChartPlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeState(
    val isLoading: Boolean = false,
    val playlists: List<DeezerChartPlaylist> = emptyList(),
    val artists: List<DeezerArtist> = emptyList(),
    val albums: List<DeezerChartAlbum> = emptyList()
)

class HomeViewModel(private val deezerDataSource: DeezerDataSource) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadContent()
    }

    private fun loadTopPlaylist() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getTopPlaylists()
                }
                _state.update { it.copy(playlists = result) }
            } catch (e: Exception) {
                Log.e("API", e.localizedMessage ?: "Unexpected playlist error")
            }
        }
    }

    private fun loadTopArtists() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getTopArtists()
                }
                _state.update { it.copy(artists = result) }
            } catch (e: Exception) {
                Log.e("API", e.localizedMessage ?: "Unexpected artists error")
            }
        }
    }

    private fun loadTopAlbums() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getTopAlbums()
                }
                _state.update { it.copy(albums = result) }
            } catch (e: Exception) {
                Log.e("API", e.localizedMessage ?: "Unexpected albums error")
            }
        }
    }

    fun loadContent() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, playlists = emptyList(), artists = emptyList(), albums = emptyList()) }
            delay(20) // Fixes stuck spinner for some reason
            loadTopPlaylist()
            loadTopArtists()
            loadTopAlbums()
            _state.update { it.copy(isLoading = false) }
        }
    }
}