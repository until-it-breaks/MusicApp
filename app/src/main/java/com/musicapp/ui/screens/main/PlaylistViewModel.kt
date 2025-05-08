package com.musicapp.ui.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerPlaylistDetailed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

data class PlaylistState(
    val id: Long? = null,
    val details: DeezerPlaylistDetailed? = null
)

class PlaylistViewModel(): ViewModel(), KoinComponent {
    private val deezerDataSource: DeezerDataSource by inject()
    private val _state = MutableStateFlow(PlaylistState())
    val state: StateFlow<PlaylistState> = _state.asStateFlow()

    suspend fun loadPlaylist(id: Long) {
        try {
            val result = deezerDataSource.getPlaylistDetails(id)
            _state.update { it.copy(details = result) }
        } catch (e: Exception) {
            Log.e("PLAYLIST", e.localizedMessage ?: "Unexpected error")
        }
    }
}