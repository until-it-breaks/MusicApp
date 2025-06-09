package com.musicapp.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.repositories.SettingsRepository
import com.musicapp.ui.models.AlbumModel
import com.musicapp.ui.models.ArtistModel
import com.musicapp.ui.models.PublicPlaylistModel
import com.musicapp.ui.models.toModel
import com.musicapp.util.getErrorMessageResId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val albums: List<AlbumModel> = emptyList(),
    val playlistErrorStringId: Int? = null,
    val artistsErrorStringId: Int? = null,
    val albumsErrorStringId: Int? = null
)

class HomeViewModel(
    private val deezerDataSource: DeezerDataSource,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun loadTopPlaylist() {
        viewModelScope.launch {
            _uiState.update { it.copy(showPlaylistLoading = true, playlistErrorStringId = null) }
            try {
                val result = withContext(Dispatchers.IO) { deezerDataSource.getTopPlaylists() }
                _uiState.update { it.copy(playlists = result.map { it.toModel() }) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(playlistErrorStringId = getErrorMessageResId(e)) }
            } finally {
                _uiState.update { it.copy(showPlaylistLoading = false) }
            }
        }
    }

    fun loadTopArtists() {
        viewModelScope.launch {
            _uiState.update { it.copy(showArtistsLoading = true, artistsErrorStringId = null) }
            try {
                val result = withContext(Dispatchers.IO) { deezerDataSource.getTopArtists() }
                _uiState.update { it.copy(artists = result.map { it.toModel() }) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(artistsErrorStringId = getErrorMessageResId(e)) }
            } finally {
                _uiState.update { it.copy(showArtistsLoading = false) }
            }
        }
    }

    fun loadTopAlbums() {
        viewModelScope.launch {
            _uiState.update { it.copy(showAlbumsLoading = true, albumsErrorStringId = null) }
            try {
                val allowExplicit = settingsRepository.allowExplicit.first()
                val result = withContext(Dispatchers.IO) { deezerDataSource.getTopAlbums() }
                /**
                 * If explicit content is allowed, the album will be shown regardless of their nature.
                 * If such setting is active only the album that are not explicit or have a null isExplicit
                 * property are displayed.
                 */
                val albums = result
                    .map { it.toModel() }
                    .filter { allowExplicit || it.isExplicit != true }
                _uiState.update { it.copy(albums = albums) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(albumsErrorStringId = getErrorMessageResId(e)) }
            } finally {
                _uiState.update { it.copy(showAlbumsLoading = false) }
            }
        }
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                playlists = emptyList(),
                artists = emptyList(),
                albums = emptyList()
            ) }
            val jobs = listOf(
                async { loadTopPlaylist() },
                async { loadTopArtists() },
                async { loadTopAlbums() }
            )
            jobs.awaitAll()
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}