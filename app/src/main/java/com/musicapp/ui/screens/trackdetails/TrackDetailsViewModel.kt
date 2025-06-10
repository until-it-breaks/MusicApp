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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TrackDetailsViewModel"

@OptIn(UnstableApi::class)
class TrackDetailsViewModel(
    mediaPlayerManager: MediaPlayerManager,
    private val authManager: AuthManager,
    private val likedTracksRepository: LikedTracksRepository
): BasePlaybackViewModel(mediaPlayerManager) {

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
}
