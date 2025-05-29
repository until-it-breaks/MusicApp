package com.musicapp.ui.screens.album

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerTrackDetailed
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.ui.models.AlbumModel
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel
import com.musicapp.playback.BasePlaybackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "AlbumViewModel"

data class AlbumState(
    val albumDetails: AlbumModel? = null,
    val tracks: List<TrackModel> = emptyList(),
    val albumDetailsAreLoading: Boolean = false,
    val tracksAreLoading: Boolean = false
)

class AlbumViewModel(
    private val deezerDataSource: DeezerDataSource,
    private val likedTracksRepository: LikedTracksRepository,
    private val auth: FirebaseAuth,
    mediaPlayerManager: MediaPlayerManager
): BasePlaybackViewModel(mediaPlayerManager) {
    private val _uiState = MutableStateFlow(AlbumState())
    val uiState: StateFlow<AlbumState> = _uiState.asStateFlow()

    fun loadAlbum(albumId: Long) {
        if (_uiState.value.albumDetails?.id == albumId) return
        viewModelScope.launch {
            _uiState.update { it.copy(albumDetailsAreLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getAlbumDetails(albumId)
                }
                _uiState.update { it.copy(albumDetails = result.toModel()) }
                loadTracks()
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                _uiState.update { it.copy(albumDetailsAreLoading = false) }
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            val tracks = uiState.value.albumDetails?.tracks.orEmpty()
            _uiState.update { it.copy(tracks = emptyList(), tracksAreLoading = true) }
            for (track in tracks) {
                try {
                    val detailedTrack: DeezerTrackDetailed = withContext(Dispatchers.IO) {
                        deezerDataSource.getTrackDetails(track.id)
                    }
                    _uiState.update { it.copy(tracks = it.tracks + detailedTrack.toModel()) }
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage, e)
                }
            }
            _uiState.update { it.copy(tracksAreLoading = false) }
        }
    }

    fun addToLiked(track: TrackModel) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                withContext(Dispatchers.IO) {
                    likedTracksRepository.addTrackToLikedTracks(userId, track)
                }
            }
        }
    }

}