package com.musicapp.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.CenteredCircularProgressIndicator
import com.musicapp.ui.composables.MainTopBar
import com.musicapp.ui.composables.PublicTrackDropDownMenu
import com.musicapp.ui.composables.TrackCard
import com.musicapp.ui.theme.AppPadding
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@UnstableApi
@Composable
fun SearchScreen(
    mainNavController: NavController,
    subNavController: NavController,
) {
    val viewModel: SearchViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackUiState by viewModel.playbackUiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            MainTopBar(
                navController = mainNavController,
                title = stringResource(R.string.search)
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(AppPadding.ScaffoldContent)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchText,
                onValueChange = viewModel::onSearchTextChange,
                placeholder = { Text(stringResource(R.string.what_to_play)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = viewModel::performSearch) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    }
                }
            )

            val searchErrorId = uiState.searchErrorStringId

            when {
                uiState.isLoading -> {
                    CenteredCircularProgressIndicator()
                }
                searchErrorId != null -> {
                    Text(
                        text = "${stringResource(R.string.search_results)} '${uiState.lastSearchText}'",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (searchErrorId != R.string.unexpected_error) {
                        Text(
                            text = stringResource(searchErrorId),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text(text = stringResource(R.string.empty_search_message))
                    }
                }
                uiState.searchResults.tracks.isNotEmpty() -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            Text(
                                text = "${stringResource(R.string.search_results)} '${uiState.lastSearchText}'",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        itemsIndexed(uiState.searchResults.tracks) { index, track ->
                            if ((index >= uiState.searchResults.tracks.size - 1) && uiState.searchResults.hasNext) {
                                viewModel.loadMoreTracks()
                            }
                            TrackCard(
                                track = track,
                                showPicture = true,
                                playbackUiState = playbackUiState,
                                onTrackClick = { scope.launch { viewModel.togglePlayback(track) } },
                                onArtistClick = { artistId -> subNavController.navigate(MusicAppRoute.Artist(artistId)) },
                                extraMenu = {
                                    PublicTrackDropDownMenu(
                                        trackModel = track,
                                        onLiked = viewModel::addToLiked,
                                        onAddToQueue = viewModel::addTrackToQueue
                                    )
                                }
                            )
                        }
                        if (uiState.isLoadingMore) {
                            item {
                                CenteredCircularProgressIndicator()
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = stringResource(R.string.discover_new_things),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}