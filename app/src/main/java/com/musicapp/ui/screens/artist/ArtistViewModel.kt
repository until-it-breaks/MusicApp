package com.musicapp.ui.screens.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.ui.models.AlbumModel
import com.musicapp.ui.models.ArtistModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ArtistState(
    val artist: ArtistModel? = null,
    val artistAlbums: List<AlbumModel> = emptyList(),
    val artistIsLoading: Boolean = false,
    val artistAlbumsAreLoading: Boolean = false,
    val error: String? = null
)

class ArtistViewModel(private val deezerDataSource: DeezerDataSource): ViewModel() {
    private val _state = MutableStateFlow(ArtistState())
    val state: StateFlow<ArtistState> = _state.asStateFlow()

    fun loadArtist(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(artistIsLoading = true, error = null) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getArtistDetails(id)
                }
                _state.update { it.copy(artist = result.toModel(), artistIsLoading = false) }
            } catch (e: Exception) {
                Log.e("ARTIST", e.localizedMessage ?: "Unexpected error loading artist")
                _state.update { it.copy(error = e.localizedMessage ?: "Unexpected error", artistIsLoading = false) }
            }
        }
    }

    fun loadArtistAlbums(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(artistAlbumsAreLoading = true, error = null) }
            try {
                val result = withContext(Dispatchers.IO)  {
                    deezerDataSource.getArtistAlbums(id)
                }
                _state.update { it.copy(artistAlbums = result.map { it.toModel() }, artistAlbumsAreLoading = false) }
            } catch (e: Exception) {
                Log.e("ARTIST", e.localizedMessage ?: "Unexpected error")
                _state.update { it.copy(error = e.localizedMessage ?: "Unexpected error", artistAlbumsAreLoading = false) }
            }
        }
    }
}