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

private const val TAG = "ArtistViewModel"

data class ArtistState(
    val artist: ArtistModel? = null,
    val artistAlbums: List<AlbumModel> = emptyList(),
    val artistIsLoading: Boolean = false,
    val artistAlbumsAreLoading: Boolean = false,
)

class ArtistViewModel(private val deezerDataSource: DeezerDataSource): ViewModel() {
    private val _state = MutableStateFlow(ArtistState())
    val state: StateFlow<ArtistState> = _state.asStateFlow()

    fun loadArtist(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(artistIsLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getArtistDetails(id)
                }
                _state.update { it.copy(artist = result.toModel()) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                _state.update { it.copy(artistIsLoading = false) }
            }
        }
    }

    fun loadArtistAlbums(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(artistAlbumsAreLoading = true) }
            try {
                val result = withContext(Dispatchers.IO)  {
                    deezerDataSource.getArtistAlbums(id)
                }
                _state.update { it.copy(artistAlbums = result.map { it.toModel() }) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                _state.update { it.copy(artistAlbumsAreLoading = false) }
            }
        }
    }
}