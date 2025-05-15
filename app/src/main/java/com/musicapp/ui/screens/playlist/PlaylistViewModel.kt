package com.musicapp.ui.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerTrackDetailed
import com.musicapp.ui.models.PlaylistModel
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlaylistState(
    val playlistDetails: PlaylistModel? = null,
    val tracks: List<TrackModel> = emptyList(),
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
                _state.update { it.copy(playlistDetails = result.toModel()) }
                loadTracks()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage ?: "Unexpected error") }
            } finally {
                _state.update { it.copy(playlistDetailsAreLoading = false) }
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            val tracks = state.value.playlistDetails?.tracks.orEmpty()
            _state.update { it.copy(tracksAreLoading = true, error = null) }
            for(track in tracks) {
                try {
                    val detailedTrack: DeezerTrackDetailed = withContext(Dispatchers.IO) {
                        deezerDataSource.getTrackDetails(track.id)
                    }
                    _state.update { it.copy(tracks = it.tracks + detailedTrack.toModel()) }
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.localizedMessage ?: "Unexpected error") }
                } finally {
                    _state.update { it.copy(tracksAreLoading = false) }
                }
            }
        }
    }
}