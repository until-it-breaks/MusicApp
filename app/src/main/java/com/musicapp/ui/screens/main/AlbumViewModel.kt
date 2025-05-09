package com.musicapp.ui.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.deezer.DeezerAlbumDetailed
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerTrackDetailed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

data class AlbumState(
    val albumDetails: DeezerAlbumDetailed? = null,
    val tracks: List<DeezerTrackDetailed> = emptyList(),
    val albumDetailsAreLoading: Boolean = false,
    val tracksAreLoading: Boolean = false,
    val error: String? = null
)

class AlbumViewModel(): ViewModel(), KoinComponent {
    private val deezerDataSource: DeezerDataSource by inject()
    private val _state = MutableStateFlow(AlbumState())
    val state: StateFlow<AlbumState> = _state.asStateFlow()

    fun loadAlbum(albumId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(albumDetailsAreLoading = true, error = null) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getAlbumDetails(albumId)
                }
                _state.update { it.copy(albumDetails = result, albumDetailsAreLoading = false) }
                loadTracks()
            } catch (e: Exception) {
                Log.e("ALBUM", e.localizedMessage ?: "Unexpected error loading album")
                _state.update { it.copy(error = e.localizedMessage ?: "Unexpected error", albumDetailsAreLoading = false) }
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            state.value.albumDetails?.tracks?.data?.let { tracks ->
                _state.update { it.copy(tracksAreLoading = true, error = null) }
                try {
                    val detailedTracks = withContext(Dispatchers.IO) {
                        tracks.map { track ->
                            deezerDataSource.getTrackDetails(track.id)
                        }
                    }
                    _state.update { it.copy(tracks = detailedTracks, tracksAreLoading = false) }
                } catch (e: Exception) {
                    Log.e("ALBUM", e.localizedMessage ?: "Unexpected error loading tracks")
                    _state.update { it.copy(error = e.localizedMessage ?: "Unexpected error", tracksAreLoading = false) }
                }
            } ?: run {
                _state.update { it.copy(tracks = emptyList(), tracksAreLoading = false) }
            }
        }
    }
}