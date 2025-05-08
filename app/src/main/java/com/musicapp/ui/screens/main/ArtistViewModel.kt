package com.musicapp.ui.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import com.musicapp.data.remote.deezer.DeezerAlbum
import com.musicapp.data.remote.deezer.DeezerArtist
import com.musicapp.data.remote.deezer.DeezerDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

data class ArtistState(
    val id: Long? = null,
    val artistDetails: DeezerArtist? = null,
    val artistAlbums: List<DeezerAlbum> = emptyList()
)

class ArtistViewModel(): ViewModel(), KoinComponent {
    private val deezerDataSource: DeezerDataSource by inject()
    private val _state = MutableStateFlow(ArtistState())
    val state: StateFlow<ArtistState> = _state.asStateFlow()

    suspend fun loadArtist(id: Long) {
        try {
            val result = deezerDataSource.getArtistDetails(id)
            _state.update { it.copy(artistDetails = result) }
        } catch (e: Exception) {
            Log.e("ARTIST", e.localizedMessage ?: "Unexpected error")
        }
    }

    suspend fun loadArtistAlbums(id: Long) {
        try {
            val result = deezerDataSource.getArtistAlbums(id)
            _state.update { it.copy(artistAlbums = result) }
        } catch (e: Exception) {
            Log.e("ARTIST", e.localizedMessage ?: "Unexpected error")
        }
    }
}