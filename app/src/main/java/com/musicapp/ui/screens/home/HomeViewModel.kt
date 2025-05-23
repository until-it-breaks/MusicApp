package com.musicapp.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.ui.models.AlbumModel
import com.musicapp.ui.models.ArtistModel
import com.musicapp.ui.models.PublicPlaylistModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "HomeViewModel"

data class HomeState(
    val isLoading: Boolean = false,
    val showPlaylistLoading: Boolean = false,
    val showArtistsLoading: Boolean = false,
    val showAlbumsLoading: Boolean = false,
    val playlists: List<PublicPlaylistModel> = emptyList(),
    val artists: List<ArtistModel> = emptyList(),
    val albums: List<AlbumModel> = emptyList()
)

class HomeViewModel(private val deezerDataSource: DeezerDataSource) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    private fun loadTopPlaylist() {
        viewModelScope.launch {
            _uiState.update { it.copy(showPlaylistLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getTopPlaylists()
                }
                _uiState.update { it.copy(playlists = result.map { it.toModel() }) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                _uiState.update { it.copy(showPlaylistLoading = false) }
            }
        }
    }

    private fun loadTopArtists() {
        viewModelScope.launch {
            _uiState.update { it.copy(showArtistsLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getTopArtists()
                }
                _uiState.update { it.copy(artists = result.map { it.toModel() }) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                _uiState.update { it.copy(showArtistsLoading = false) }
            }
        }
    }

    private fun loadTopAlbums() {
        viewModelScope.launch {
            _uiState.update { it.copy(showAlbumsLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getTopAlbums()
                }
                _uiState.update { it.copy(albums = result.map { it.toModel() }) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                _uiState.update { it.copy(showAlbumsLoading = false) }
            }
        }
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, playlists = emptyList(), artists = emptyList(), albums = emptyList()) }
            val jobs = listOf(
                async { loadTopPlaylist() },
                async { loadTopArtists() },
                async { loadTopAlbums() }
            )
            jobs.awaitAll()
            delay(20) // Fixes stuck spinner
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}