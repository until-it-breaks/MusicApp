package com.musicapp.ui.screens.playlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerTrackDetailed
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.ui.models.PublicPlaylistModel
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "PublicPlaylistViewModel"

data class PublicPlaylistState(
    val playlistDetails: PublicPlaylistModel? = null,
    val tracks: List<TrackModel> = emptyList(),
    val showPlaylistDetailsLoading: Boolean = false,
    val showTracksLoading: Boolean = false
)

class PublicPlaylistViewModel(
    private val deezerDataSource: DeezerDataSource,
    private val auth: FirebaseAuth,
    private val likedTracksRepository: LikedTracksRepository,
    private val mediaPlayerManager: MediaPlayerManager
): ViewModel() {
    private val _uiState = MutableStateFlow(PublicPlaylistState())
    val uiState: StateFlow<PublicPlaylistState> = _uiState.asStateFlow()

    fun loadPlaylist(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(showPlaylistDetailsLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getPlaylistDetails(id)
                }
                _uiState.update { it.copy(playlistDetails = result.toModel()) }
                loadTracks()
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
            } finally {
                _uiState.update { it.copy(showPlaylistDetailsLoading = false) }
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            val tracks = uiState.value.playlistDetails?.tracks.orEmpty().take(20) // Load only 20 tracks.
            _uiState.update { it.copy(showTracksLoading = true) }
            for(track in tracks) {
                try {
                    val detailedTrack: DeezerTrackDetailed = withContext(Dispatchers.IO) {
                        deezerDataSource.getTrackDetails(track.id)
                    }
                    _uiState.update { it.copy(tracks = it.tracks + detailedTrack.toModel()) }
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage, e)
                } finally {
                    _uiState.update { it.copy(showTracksLoading = false) }
                }
            }
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

    fun addToQueue(track: TrackModel) {
        viewModelScope.launch {
            // TODO Enqueue given track
        }
    }

    fun playTrack(track: TrackModel) {
        mediaPlayerManager.togglePlayback(track)
    }
}