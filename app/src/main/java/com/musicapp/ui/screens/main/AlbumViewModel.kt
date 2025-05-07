package com.musicapp.ui.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import com.musicapp.data.remote.deezer.DeezerAlbumDetails
import com.musicapp.data.remote.deezer.DeezerDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

data class AlbumState(
    val id: Long? = null,
    val details: DeezerAlbumDetails? = null
)

class AlbumViewModel(): ViewModel(), KoinComponent {
    private val deezerDataSource: DeezerDataSource by inject()
    private val _state = MutableStateFlow(AlbumState())
    val state: StateFlow<AlbumState> = _state.asStateFlow()

    suspend fun loadAlbum(id: Long) {
        try {
            val result = deezerDataSource.getAlbumDetails(id)
            _state.update { it.copy(details = result) }
        } catch (e: Exception) {
            Log.e("ALBUM", e.localizedMessage ?: "Unexpected error")
        }
    }
}