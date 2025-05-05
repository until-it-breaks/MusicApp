package com.musicapp.ui.screens.main.home

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.DeezerDataSource
import com.musicapp.data.remote.DeezerPlaylist
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel: ViewModel(), KoinComponent {
    private val deezerDataSource: DeezerDataSource by inject()
    private val _playlists = mutableStateOf<List<DeezerPlaylist>>(emptyList())
    val playlists: MutableState<List<DeezerPlaylist>> = _playlists

    init {
        loadTopPlaylist()
    }

    private fun loadTopPlaylist() {
        viewModelScope.launch {
            try {
                val result = deezerDataSource.getTopPlaylists()
                _playlists.value = result
            } catch (e: Exception) {
                Log.e(null, e.localizedMessage ?: "Unexpected error")
            }
        }
    }
}