package com.musicapp.ui.screens.playlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.repositories.PlaylistsRepository
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.UserPlaylistModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersonalPlaylistViewModel(private val playlistsRepository: PlaylistsRepository): ViewModel() {
    private val _selectedPlaylistId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlist: StateFlow<UserPlaylistModel?> = _selectedPlaylistId
        .filterNotNull()
        .flatMapLatest { playlistId ->
            playlistsRepository.getUserPlaylistWithTracks(playlistId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    fun loadPlaylistTracks(playlistId: String) {
        _selectedPlaylistId.value = playlistId
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            val playlistId = _selectedPlaylistId.value
            playlistId?.let {
                withContext(Dispatchers.IO) {
                    playlistsRepository.clearPlaylist(it) // Find way to delete it and navigate away
                }
            }
        }
    }

    fun removeTrackFromPlaylist(trackId: Long) {
        viewModelScope.launch {
            val playlistId = _selectedPlaylistId.value
            playlistId?.let {
                try {
                    withContext(Dispatchers.IO) {
                        playlistsRepository.removeTrackFromPlaylist(it, trackId)
                    }
                } catch (e: Exception) {
                    Log.e("LikedTracksViewModel", "Error removing track from liked tracks: ${e.localizedMessage}", e)
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
        viewModelScope.launch {
            // TODO Play given track
        }
    }
}