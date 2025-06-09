package com.musicapp.ui.screens.search

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.data.repositories.SettingsRepository
import com.musicapp.playback.BasePlaybackViewModel
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel
import com.musicapp.ui.screens.AuthManager
import com.musicapp.util.getErrorMessageResId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SearchViewModel"

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
    val searchErrorStringId: Int? = null,
    val paginationErrorStringId: Int? = null
)

@UnstableApi
class SearchViewModel(
    private val deezerDataSource: DeezerDataSource,
    private val likedTracksRepository: LikedTracksRepository,
    private val settingsRepository: SettingsRepository,
    private val authManager: AuthManager,
    mediaPlayerManager: MediaPlayerManager
) : BasePlaybackViewModel(mediaPlayerManager) {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onSearchTextChange(newText: String) {
        _uiState.update { it.copy(searchText = newText) }
    }

    fun performSearch() {
        val currentSearchText = _uiState.value.searchText

        _uiState.update {
            it.copy(
                lastSearchText = currentSearchText,
                searchErrorStringId = null,
                paginationErrorStringId = null
            )
        }

        if (currentSearchText.isBlank()) {
            _uiState.update {
                it.copy(
                    searchResults = SearchResultsUiState(),
                    searchErrorStringId = null,
                    paginationErrorStringId = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val allowExplicit = settingsRepository.allowExplicit.first()

            try {
                val results = withContext(Dispatchers.IO) {
                    deezerDataSource.getSearchTracks(currentSearchText)
                }

                val trackModels = results.data
                    .filter { allowExplicit || it.explicitLyrics != true }
                    .map { it.toModel() }

                _uiState.update {
                    it.copy(
                        searchResults = SearchResultsUiState(
                            tracks = trackModels,
                            totalResults = results.total,
                            hasNext = results.next != null,
                            next = results.next
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error performing search for '$currentSearchText': ${e.message}", e)
                _uiState.update {
                    it.copy(searchErrorStringId = getErrorMessageResId(e))
                }
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun loadMoreTracks() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.searchResults.hasNext || state.searchResults.next == null) {
            return
        }

        val nextUrl = state.searchResults.next

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, paginationErrorStringId = null) }

            val allowExplicit = settingsRepository.allowExplicit.first()

            try {
                val results = withContext(Dispatchers.IO) {
                    deezerDataSource.getTracksByUrl(nextUrl)
                }
                val newTrackModels = results.data
                    .filter { allowExplicit || it.explicitLyrics != true }
                    .map { it.toModel() }

                _uiState.update {
                    it.copy(
                        searchResults = it.searchResults.copy(
                            tracks = it.searchResults.tracks + newTrackModels,
                            hasNext = results.next != null,
                            next = results.next
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading next page from $nextUrl: ${e.message}", e)
                _uiState.update { it.copy(paginationErrorStringId = getErrorMessageResId(e)) }
            } finally {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun addToLiked(track: TrackModel) {
        viewModelScope.launch {
            authManager.userId.value?.let {
                try {
                    withContext(Dispatchers.IO) {
                        likedTracksRepository.addTrackToLikedTracks(it, track)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage, e)
                }
            }
        }
    }
}