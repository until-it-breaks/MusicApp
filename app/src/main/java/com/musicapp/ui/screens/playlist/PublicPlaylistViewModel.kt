package com.musicapp.ui.screens.playlist

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.R
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerTrackDetailed
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.data.repositories.SettingsRepository
import com.musicapp.playback.BasePlaybackViewModel
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.ui.models.PublicPlaylistModel
import com.musicapp.ui.models.TrackModel
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

private const val TAG = "PublicPlaylistViewModel"

data class PublicPlaylistState(
    val playlistDetails: PublicPlaylistModel? = null,
    val tracks: List<TrackModel> = emptyList(),
    val showPlaylistDetailsLoading: Boolean = false,
    val showTracksLoading: Boolean = false,
    val playlistErrorStringId: Int? = null,
    val tracksErrorStringId: Int? = null,
    val failedTracksCount: Int = 0
)

class PublicPlaylistViewModel(
    private val deezerDataSource: DeezerDataSource,
    private val auth: FirebaseAuth,
    private val likedTracksRepository: LikedTracksRepository,
    private val settingsRepository: SettingsRepository,
    mediaPlayerManager: MediaPlayerManager
) : BasePlaybackViewModel(mediaPlayerManager) {
    private val _uiState = MutableStateFlow(PublicPlaylistState())
    val uiState: StateFlow<PublicPlaylistState> = _uiState.asStateFlow()

    fun loadPlaylist(id: Long) {
        if (_uiState.value.playlistDetails?.id == id) return
        viewModelScope.launch {
            _uiState.update { it.copy(showPlaylistDetailsLoading = true, playlistErrorStringId = null) }
            try {
                val result = withContext(Dispatchers.IO) {
                    deezerDataSource.getPlaylistDetails(id)
                }
                _uiState.update { it.copy(playlistDetails = result.toModel()) }
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
                _uiState.update { it.copy(playlistErrorStringId = getErrorMessageResId(e)) }
            } finally {
                _uiState.update { it.copy(showPlaylistDetailsLoading = false) }
                loadTracks()
            }
        }
    }

    fun loadTracks() {
        viewModelScope.launch {
            val tracks = uiState.value.playlistDetails?.tracks.orEmpty().take(20) // Load 20 tracks at most due to API limitations.
            _uiState.update {
                it.copy(
                    showTracksLoading = true,
                    tracksErrorStringId = null,
                    tracks = emptyList()
                )
            }
            val allowExplicit = settingsRepository.allowExplicit.first()

            var failedCount = 0
            for (track in tracks) {
                try {
                    val detailedTrack: DeezerTrackDetailed = withContext(Dispatchers.IO) {
                        deezerDataSource.getTrackDetails(track.id)
                    }
                    val trackModel = detailedTrack.toModel()
                    /**
                     * If explicit content is allowed, the track will be shown regardless of their nature.
                     * If such setting is active only the tracks that are not explicit or have a null isExplicit
                     * property are displayed.
                     */
                    if (allowExplicit || trackModel.isExplicit != true) {
                        _uiState.update { it.copy(tracks = it.tracks + trackModel) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage, e)
                    failedCount++
                }
            }
            _uiState.update {
                it.copy(
                    showTracksLoading = false,
                    tracksErrorStringId = if (failedCount > 0) R.string.partial_track_load_error else null,
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