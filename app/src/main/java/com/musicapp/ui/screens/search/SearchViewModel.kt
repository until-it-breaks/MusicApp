package com.musicapp.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.musicapp.data.remote.deezer.DeezerDataSource

import com.musicapp.data.remote.deezer.DeezerSearchResponse
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.playback.PlaybackUiState

import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

import kotlinx.coroutines.launch

import android.util.Log
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class SearchResultsUiState(
    val tracks: List<TrackModel> = emptyList(),
    val totalResults: Int = 0,
    val hasNext: Boolean = false,
    val next: String? = null
)

data class SearchUiState(
    val searchText: String = "",
    val lastSearchText: String = "",
    val searchResults: SearchResultsUiState = SearchResultsUiState(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: Boolean = false,
    val paginationError: Boolean = false,
    val playbackState: PlaybackUiState = PlaybackUiState()
)

class SearchViewModel(
    private val deezerDataSource: DeezerDataSource,
    private val mediaPlayerManager: MediaPlayerManager
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    private val _lastSearchText = MutableStateFlow("")
    private val _searchResultsState = MutableStateFlow(SearchResultsUiState())
    private val _isLoading = MutableStateFlow(false)
    private val _isLoadingMore = MutableStateFlow(false)
    private val _error = MutableStateFlow(false)
    private val _paginationError = MutableStateFlow(false)

    val uiState: StateFlow<SearchUiState> = combine(
        // Provide the flows as a List explicitly because for some reason it can't infer the types
        listOf(
            _searchText,
            _lastSearchText,
            _searchResultsState,
            _isLoading,
            _isLoadingMore,
            _error,
            _paginationError,
            mediaPlayerManager.playbackState
        )
    ) { values ->
        val searchText = values[0] as String
        val lastSearchText = values[1] as String
        val searchResults = values[2] as SearchResultsUiState
        val isLoading = values[3] as Boolean
        val isLoadingMore = values[4] as Boolean
        val error = values[5] as Boolean
        val paginationError = values[6] as Boolean
        val playbackState = values[7] as PlaybackUiState

        SearchUiState(
            searchText = searchText,
            lastSearchText = lastSearchText,
            searchResults = searchResults,
            isLoading = isLoading,
            isLoadingMore = isLoadingMore,
            error = error,
            paginationError = paginationError,
            playbackState = playbackState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L), // Strategy for sharing the state
        initialValue = SearchUiState()
    )


    fun onSearchTextChange(newText: String) {
        _searchText.value = newText
    }

    fun performSearch() {
        val currentSearchText = _searchText.value
        _lastSearchText.value = currentSearchText

        if (currentSearchText.isBlank()) {
            _searchResultsState.value = SearchResultsUiState()
            _error.value = false
            _paginationError.value = false
            mediaPlayerManager.stop()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = false
            _paginationError.value = false
            mediaPlayerManager.stop()

            try {
                val results: DeezerSearchResponse =
                    deezerDataSource.getSearchTracks(currentSearchText)
                val trackModels = results.data.map { it.toModel() }

                _searchResultsState.value = SearchResultsUiState(
                    tracks = trackModels,
                    totalResults = results.total,
                    hasNext = results.next != null,
                    next = results.next
                )
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(
                    "SearchViewModel",
                    "Error performing initial search for '$currentSearchText': ${e.message}",
                    e
                )
                _isLoading.value = false
                _error.value = true
            }
        }
    }

    fun loadMoreTracks() {
        if (_isLoadingMore.value || !_searchResultsState.value.hasNext || _searchResultsState.value.next == null) {
            return
        }

        val nextUrl = _searchResultsState.value.next!!

        viewModelScope.launch {
            _isLoadingMore.value = true
            _paginationError.value = false

            try {
                val results: DeezerSearchResponse = deezerDataSource.getTracksByUrl(nextUrl)
                val newTrackModels = results.data.map { it.toModel() }

                _searchResultsState.update { current ->
                    current.copy(
                        tracks = current.tracks + newTrackModels, // Append new tracks
                        hasNext = results.next != null,
                        next = results.next // Update with the URL for the subsequent page
                    )
                }
                _isLoadingMore.value = false
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error loading next page from $nextUrl: ${e.message}", e)
                _isLoadingMore.value = false
                _paginationError.value = true
            }
        }
    }

    fun playTrack(track: TrackModel) {
        mediaPlayerManager.togglePlayback(track)
    }

    fun stopMusic() {
        mediaPlayerManager.stop()
    }
}

