package com.musicapp.ui.screens.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerTrackDetailed
import com.musicapp.data.repositories.PlaylistsRepository
import com.musicapp.ui.models.AlbumModel
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AlbumState(
    val albumDetails: AlbumModel? = null,
    val tracks: List<TrackModel> = emptyList(),
    val albumDetailsAreLoading: Boolean = false,
    val tracksAreLoading: Boolean = false,
    val error: String? = null
)

class AlbumViewModel(
    private val deezerDataSource: DeezerDataSource,
    private val playlistsRepository: PlaylistsRepository,
    private val auth: FirebaseAuth
): ViewModel() {
    private val _state = MutableStateFlow(AlbumState())
    val state: StateFlow<AlbumState> = _state.asStateFlow()

    fun loadAlbum(albumId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(albumDetailsAreLoading = true, error = null) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getAlbumDetails(albumId)
                }
                _state.update { it.copy(albumDetails = result.toModel()) }
                loadTracks()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage ?: "Unexpected error") }
            } finally {
                _state.update { it.copy(albumDetailsAreLoading = false) }
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            val tracks = state.value.albumDetails?.tracks.orEmpty()
            _state.update { it.copy(tracks = emptyList(), tracksAreLoading = true, error = null) }
            for (track in tracks) {
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

    fun addToLiked(track: TrackModel) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                withContext(Dispatchers.IO) {
                    playlistsRepository.addTrackToLikedTracksPlaylist(userId, track)
                }
            }
        }
    }
}