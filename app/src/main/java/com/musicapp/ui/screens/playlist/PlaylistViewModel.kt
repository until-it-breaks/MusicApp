package com.musicapp.ui.screens.playlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerPlaylistDetailed
import com.musicapp.data.remote.deezer.DeezerTrackDetailed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlaylistState(
    val playlistDetails: DeezerPlaylistDetailed? = null,
    val tracks: List<DeezerTrackDetailed> = emptyList(),
    val playlistDetailsAreLoading: Boolean = false,
    val tracksAreLoading: Boolean = false,
    val error: String? = null
)

class PlaylistViewModel(private val deezerDataSource: DeezerDataSource): ViewModel() {
    private val _state = MutableStateFlow(PlaylistState())
    val state: StateFlow<PlaylistState> = _state.asStateFlow()

    fun loadPlaylist(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(playlistDetailsAreLoading = true, error = null) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getPlaylistDetails(id)
                }
                _state.update { it.copy(playlistDetails = result, playlistDetailsAreLoading = false) }
                loadTracks()
            } catch (e: Exception) {
                Log.e("PLAYLIST", e.localizedMessage ?: "Unexpected error")
                _state.update { it.copy(error = e.localizedMessage ?: "Unexpected error", playlistDetailsAreLoading = false) }
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            state.value.playlistDetails?.tracks?.data?.let { tracks ->
                _state.update { it.copy(tracksAreLoading = true, error = null) }
                try {
                    val detailedTracks = withContext(Dispatchers.IO) {
                        tracks.map { track ->
                            deezerDataSource.getTrackDetails(track.id)
                        }
                    }
                    _state.update { it.copy(tracks = detailedTracks, tracksAreLoading = false) }
                } catch (e: Exception) {
                    Log.e("PLAYLIST", e.localizedMessage ?: "Unexpected error loading tracks")
                    _state.update { it.copy(error = e.localizedMessage ?: "Unexpected error", tracksAreLoading = false) }
                }
            } ?: run {
                _state.update { it.copy(tracks = emptyList(), tracksAreLoading = false) }
            }
        }
    }
}