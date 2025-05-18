package com.musicapp.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.data.remote.deezer.DeezerChartTrack
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val searchText: String = "",
    val searchResults: List<TrackModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchViewModel(private val deezerDataSource: DeezerDataSource) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<String>("")
    val searchResults: StateFlow<String> = _searchResults

    fun onSearchTextChange(newText: String) {
        _uiState.update { it.copy(searchText = newText) }
    }

    fun performSearch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val results = deezerDataSource.getSearchTracks(_uiState.value.searchText)

                _uiState.update {
                    it.copy(
                        searchResults = results.map { it.toModel() },
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to search tracks"
                    )
                }
            }
        }
    }

}
