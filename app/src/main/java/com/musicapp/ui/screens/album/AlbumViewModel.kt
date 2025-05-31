package com.musicapp.ui.screens.album

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.R
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerTrackDetailed
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.playback.BasePlaybackViewModel
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.ui.models.AlbumModel
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel
import com.musicapp.util.getErrorMessageResId
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
    val showAlbumDetailsLoading: Boolean = false,
    val showTracksLoading: Boolean = false,
    val albumErrorStringId: Int? = null,
    val tracksErrorStringId: Int? = null,
    val failedTracksCount: Int = 0
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
            _uiState.update { it.copy(showAlbumDetailsLoading = true, albumErrorStringId = null) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getAlbumDetails(albumId)
                }
                _uiState.update { it.copy(albumDetails = result.toModel()) }
                loadTracks()
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(albumErrorStringId = getErrorMessageResId(e)) }
            } finally {
                _uiState.update { it.copy(showAlbumDetailsLoading = false) }
            }
        }
    }

    fun loadTracks() {
        viewModelScope.launch {
            val tracks = uiState.value.albumDetails?.tracks.orEmpty()
            _uiState.update { it.copy(tracks = emptyList(), showTracksLoading = true, tracksErrorStringId = null, failedTracksCount = 0) }

            var failedCount = 0
            for (track in tracks) {
                try {
                    val detailedTrack: DeezerTrackDetailed = withContext(Dispatchers.IO) {
                        deezerDataSource.getTrackDetails(track.id)
                    }
                    _uiState.update { it.copy(tracks = it.tracks + detailedTrack.toModel()) }
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage, e)
                    failedCount++
                }
            }
            _uiState.update {
                it.copy(
                    showTracksLoading = false,
                    tracksErrorStringId = if (failedCount > 0) R.string.track_load_error else null,
                    failedTracksCount = failedCount
                )
            }
        }
    }

    fun addToLiked(track: TrackModel) {
        viewModelScope.launch {
            auth.currentUser?.uid?.let {
                try {
                    withContext(Dispatchers.IO) {
                        likedTracksRepository.addTrackToLikedTracks(it, track)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage, e)
                }
            }
        }
    }
}