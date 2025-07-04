package com.musicapp.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.musicapp.ui.composables.CenteredLinearProgressIndicator
import com.musicapp.ui.composables.ErrorSection
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.PublicTrackDropDownMenu
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TrackCard
import com.musicapp.ui.theme.AppPadding
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicPlaylistScreen(navController: NavController, playlistId: Long) {
    val viewModel = koinViewModel<PublicPlaylistViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackUiState by viewModel.playbackUiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

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
                .padding(AppPadding.ScaffoldContent)
        ) {
            item {
                if (uiState.showPlaylistDetailsLoading) {
                    CenteredCircularProgressIndicator()
                }
                uiState.playlistErrorStringId?.let {
                    ErrorSection(
                        title = stringResource(R.string.failed_to_load_playlist),
                        message = stringResource(it),
                        onRetry = { viewModel.loadPlaylist(playlistId) }
                    )
                }
            }
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
            item {
                if (uiState.showTracksLoading) {
                    CenteredLinearProgressIndicator()
                }
                uiState.tracksErrorStringId?.let {
                    ErrorSection(
                        title = stringResource(R.string.failed_to_load_tracks),
                        message = stringResource(it) + if (uiState.failedTracksCount > 0) " (${uiState.failedTracksCount} ${stringResource(R.string.tracks)})" else "",
                        onRetry = { viewModel.loadTracks() }
                    )
                }
            }
            itemsIndexed(uiState.tracks) { index, track ->
                TrackCard(
                    track = track,
                    showPicture = true,
                    playbackUiState = playbackUiState,
                    onTrackClick = { scope.launch { viewModel.setPlaybackQueue(uiState.tracks, index) } },
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