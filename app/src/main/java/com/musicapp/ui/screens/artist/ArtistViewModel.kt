package com.musicapp.ui.screens.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.R
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.repositories.SettingsRepository
import com.musicapp.ui.models.AlbumModel
import com.musicapp.ui.models.ArtistModel
import com.musicapp.ui.models.toModel
import com.musicapp.util.getErrorMessageResId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ArtistViewModel"

data class ArtistState(
    val artist: ArtistModel? = null,
    val artistAlbums: List<AlbumModel> = emptyList(),
    val isArtistLoaded: Boolean = false,
    val areAlbumsLoaded: Boolean = false,
    val showArtistLoading: Boolean = false,
    val showAlbumsLoading: Boolean = false,
    val artistErrorStringId: Int? = null,
    val albumErrorStringId: Int? = null,
)

class ArtistViewModel(
    private val deezerDataSource: DeezerDataSource,
    private val settingsRepository: SettingsRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ArtistState())
    val uiState: StateFlow<ArtistState> = _uiState.asStateFlow()

    fun loadArtist(id: Long) {
        if (_uiState.value.isArtistLoaded && _uiState.value.artist?.id == id) return
        viewModelScope.launch {
            _uiState.update { it.copy(showArtistLoading = true, artistErrorStringId = null) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getArtistDetails(id)
                }
                _uiState.update { it.copy(artist = result.toModel(), isArtistLoaded = true) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
                val resId: Int = getErrorMessageResId(e) ?: R.string.failed_to_load_artist
                _uiState.update { it.copy(artistErrorStringId = resId) }
            } finally {
                _uiState.update { it.copy(showArtistLoading = false) }
            }
        }
    }

    fun loadArtistAlbums(id: Long) {
        if (_uiState.value.areAlbumsLoaded && _uiState.value.artist?.id == id) return
        viewModelScope.launch {
            _uiState.update { it.copy(showAlbumsLoading = true, albumErrorStringId = null) }
            try {
                val allowExplicit = settingsRepository.allowExplicit.first()
                val result = withContext(Dispatchers.IO)  {
                    deezerDataSource.getArtistAlbums(id)
                }
                /**
                 * If explicit content is allowed, the album will be shown regardless of their nature.
                 * If such setting is active only the album that are not explicit or have a null isExplicit
                 * property are displayed.
                 */
                val albums = result
                    .map { it.toModel() }
                    .filter { allowExplicit || it.isExplicit != true }
                _uiState.update { it.copy(artistAlbums = albums, areAlbumsLoaded = true) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
                val resId: Int = getErrorMessageResId(e) ?: R.string.failed_to_load_albums
                _uiState.update { it.copy(albumErrorStringId = resId) }
            } finally {
                _uiState.update { it.copy(showAlbumsLoading = false) }
            }
        }
    }
}