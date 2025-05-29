package com.musicapp.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.PublicTrackDropDownMenu
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TrackCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicPlaylistScreen(navController: NavController, playlistId: Long) {
    val viewModel = koinViewModel<PublicPlaylistViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    Scaffold(
        topBar = { TopBarWithBackButton(navController, title = stringResource(R.string.playlist_details)) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
        ) {
            item {
                uiState.playlistDetails?.let { playlist ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LoadableImage(
                                imageUri = playlist.bigPictureUri,
                                contentDescription = null
                            )
                        }
                        Text(
                            text = playlist.title,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        playlist.description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        playlist.creator?.name?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            itemsIndexed(uiState.tracks) { index, track ->
                TrackCard(
                    track = track,
                    showPicture = true,
                    onTrackClick = { viewModel.setPlaybackQueue(uiState.tracks, index) },
                    onArtistClick = { artistId -> navController.navigate(MusicAppRoute.Artist(artistId)) },
                    extraMenu = {
                        PublicTrackDropDownMenu(
                            trackModel = track,
                            onLiked = viewModel::addToLiked,
                            onAddToQueue = viewModel::addTrackToQueue
                        )
                    }
                )
            }
        }
    }
}