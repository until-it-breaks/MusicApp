package com.musicapp.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import org.koin.androidx.compose.koinViewModel
import com.musicapp.ui.composables.ArtistCard
import com.musicapp.ui.composables.CenteredCircularProgressIndicator
import com.musicapp.ui.composables.MainTopBar
import com.musicapp.ui.composables.PlayListCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mainNavController: NavController, subNavController: NavController) {
    val viewModel = koinViewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = { MainTopBar(mainNavController, stringResource(R.string.home)) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = viewModel::loadContent,
            state = pullToRefreshState,
            modifier = Modifier.padding(contentPadding)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize()
            ) {
                item {
                    Text(
                        text = stringResource(R.string.top_playlists),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                item {
                    if (uiState.showPlaylistLoading) {
                        CenteredCircularProgressIndicator()
                    }
                }
                items(uiState.playlists.chunked(2)) { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { playlist ->
                            PlayListCard(
                                title = playlist.title,
                                imageUri = playlist.mediumPictureUri,
                                modifier = Modifier.weight(1f),
                                onClick = { subNavController.navigate(MusicAppRoute.Playlist(playlist.id)) }
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                item {
                    Text(
                        text = stringResource(R.string.top_artists),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                item {
                    if (uiState.showArtistsLoading) {
                        CenteredCircularProgressIndicator()
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.artists) { artist ->
                            ArtistCard(
                                title = artist.name,
                                imageUrl = artist.mediumPictureUri,
                                onClick = { subNavController.navigate(MusicAppRoute.Artist(artist.id)) }
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = stringResource(R.string.top_albums),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                item {
                    if (uiState.showAlbumsLoading) {
                        CenteredCircularProgressIndicator()
                    }
                }
                items(uiState.albums.chunked(2)) { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            PlayListCard(
                                title = item.title,
                                imageUri = item.mediumCoverUri,
                                modifier = Modifier.weight(1f),
                                onClick = { subNavController.navigate(MusicAppRoute.Album(item.id)) }
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}