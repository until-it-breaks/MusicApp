package com.musicapp.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.remote.deezer.DeezerSearchResponse
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchResultsUiState(
    val tracks: List<TrackModel> = emptyList(),
    val totalResults: Int = 0,
    val hasNext: Boolean = false,
    val next: String? = null
)

data class SearchUiState(
    val searchText: String = "",
    val searchResults: SearchResultsUiState = SearchResultsUiState(), // Use the simplified UI state
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: Boolean = false,
    val paginationError: Boolean = false
)

class SearchViewModel(private val deezerDataSource: DeezerDataSource) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()


    fun onSearchTextChange(newText: String) {
        _uiState.update { it.copy(searchText = newText) }
    }

    fun performSearch() {
        val currentSearchText = _uiState.value.searchText
        if (currentSearchText.isBlank()) {
            _uiState.update {
                it.copy(
                    searchResults = SearchResultsUiState(),
                    error = false,
                    paginationError = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = false,
                    paginationError = false
                )
            }

            try {
                val results: DeezerSearchResponse =
                    deezerDataSource.getSearchTracks(currentSearchText)
                val trackModels = results.data.map { it.toModel() }

                _uiState.update {
                    it.copy(
                        searchResults = SearchResultsUiState(
                            tracks = trackModels,
                            totalResults = results.total,
                            hasNext = results.next != null,
                            next = results.next
                        ),
                        isLoading = false,
                        error = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = true
                    )
                }
            }
        }
    }

    fun loadMoreTracks() {
        if (_uiState.value.isLoadingMore || !_uiState.value.searchResults.hasNext || _uiState.value.searchResults.next == null) {
            return
        }

        val nextUrl = _uiState.value.searchResults.next!!

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, paginationError = false) }

            try {
                val results: DeezerSearchResponse = deezerDataSource.getTracksByUrl(nextUrl)
                val newTrackModels = results.data.map { it.toModel() }

                _uiState.update { currentState ->
                    currentState.copy(
                        searchResults = currentState.searchResults.copy(
                            tracks = currentState.searchResults.tracks + newTrackModels,
                            hasNext = results.next != null,
                            next = results.next
                        ),
                        isLoadingMore = false,
                        paginationError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        paginationError = true
                    )
                }
            }
        }
    }
}
