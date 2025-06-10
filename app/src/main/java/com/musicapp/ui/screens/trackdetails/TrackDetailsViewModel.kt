package com.musicapp.ui.screens.trackdetails

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.musicapp.auth.AuthManager
import com.musicapp.data.models.TrackModel
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.playback.BasePlaybackViewModel
import com.musicapp.playback.MediaPlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TrackDetailsViewModel"

@OptIn(UnstableApi::class)
class TrackDetailsViewModel(
    mediaPlayerManager: MediaPlayerManager,
    private val authManager: AuthManager,
    private val likedTracksRepository: LikedTracksRepository
) : BasePlaybackViewModel(mediaPlayerManager) {

    private var _isCurrentTrackLiked = MutableStateFlow(false)
    val isCurrentTrackLiked = _isCurrentTrackLiked.asStateFlow()

    init {
        observeCurrentTrackLikedStatus()
    }

    override fun onCleared() {
        super.onCleared()
        authManager.cleanup()
    }

    fun toggleAddToLiked(track: TrackModel) {
        viewModelScope.launch {
            authManager.userId.value?.let {
                try {
                    withContext(Dispatchers.IO) {
                        if (!likedTracksRepository.isTrackInLikedTracks(it, track)) {
                            likedTracksRepository.addTrackToLikedTracks(it, track)
                        } else {
                            likedTracksRepository.removeTrackFromLikedTracks(it, track)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage, e)
                }
            }
        }
    }


    private fun observeCurrentTrackLikedStatus() {
        viewModelScope.launch {
            authManager.userId
                .collect { userId ->
                    if (userId == null) {
                        _isCurrentTrackLiked.value = false
                        return@collect
                    }

                    likedTracksRepository.getPlaylistWithTracks(userId)
                        .map { likedPlaylist ->
                            val currentTrackId = playbackUiState.value.currentQueueItem?.track?.id
                            if (currentTrackId == null) {
                                false
                            } else {
                                likedPlaylist.tracks.any { likedTrack ->
                                    likedTrack.trackId == currentTrackId
                                }
                            }
                        }
                        .distinctUntilChanged() // Only emit if liked status actually changes
                        .collect { isLiked ->
                            _isCurrentTrackLiked.value = isLiked
                            Log.d(TAG, "Current track liked status updated to: $isLiked")
                        }
                }
        }
    }
}
