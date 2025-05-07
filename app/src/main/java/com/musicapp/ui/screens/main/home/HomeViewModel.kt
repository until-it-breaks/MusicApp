package com.musicapp.ui.screens.main.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.deezer.DeezerAlbum
import com.musicapp.data.remote.deezer.DeezerArtist
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerPlaylist
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    val artists: List<DeezerArtist> = emptyList(),
    val albums: List<DeezerAlbum> = emptyList()
)

class HomeViewModel : ViewModel(), KoinComponent {
    private val deezerDataSource: DeezerDataSource by inject()
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        onRefresh()
    }

    private suspend fun loadTopPlaylist() {
        try {
            val result = deezerDataSource.getTopPlaylists()
            _state.update { it.copy(playlists = result.shuffled()) }
        } catch (e: Exception) {
            Log.e("API", e.localizedMessage ?: "Unexpected playlist error")
        }
    }

    private suspend fun loadTopArtists() {
        try {
            val result = deezerDataSource.getTopArtists()
            _state.update { it.copy(artists = result.shuffled()) }
        } catch (e: Exception) {
            Log.e("API", e.localizedMessage ?: "Unexpected artists error")
        }
    }

    private suspend fun loadTopAlbums() {
        try {
            val result = deezerDataSource.getTopAlbums()
            _state.update { it.copy(albums = result.shuffled()) }
        } catch (e: Exception) {
            Log.e("API", e.localizedMessage ?: "Unexpected albums error")
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true, playlists = emptyList(), artists = emptyList(), albums = emptyList()) }
            awaitAll(
                async { loadTopPlaylist() },
                async { loadTopArtists() },
                async { loadTopAlbums() }
            )
            _state.update { it.copy(refreshing = false) }
        }
    }
}