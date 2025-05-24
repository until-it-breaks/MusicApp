    package com.musicapp.ui.screens.search

    import androidx.compose.ui.res.stringResource
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.musicapp.R

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
        val tracks: List<TrackModel> = emptyList(), // Store UI-ready TrackModels
        val totalResults: Int = 0, // Optionally, store total results
        val hasMore: Boolean = false // Indicates if there's a next page
    )

    data class SearchUiState(
        val searchText: String = "",
        val searchResults: SearchResultsUiState = SearchResultsUiState(), // Use the simplified UI state
        val isLoading: Boolean = false,
        val error: Boolean = false
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
                _uiState.update { it.copy(searchResults = SearchResultsUiState(), error = false) }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = false) }

                try {
                    val results: DeezerSearchResponse = deezerDataSource.getSearchTracks(currentSearchText)
                    val trackModels = results.data.map { it.toModel() }

                    _uiState.update {
                        it.copy(
                            searchResults = SearchResultsUiState(
                                tracks = trackModels,
                                totalResults = results.total,
                                hasMore = results.next != null
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
    }
