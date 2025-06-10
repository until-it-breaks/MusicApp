package com.musicapp.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.AuthErrorMessage
import com.musicapp.ui.composables.LikedTracksPlaylistDropDownMenu
import com.musicapp.ui.composables.SavedTrackDropDownMenu
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TrackCard
import com.musicapp.ui.theme.AppPadding
import com.musicapp.util.convertMillisToDateWithHourAndMinutes
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@UnstableApi
@Composable
fun LikedTracksScreen(mainNavController: NavController, subNavController: NavController) {
    val viewModel = koinViewModel<LikedTracksViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playlist by viewModel.playlist.collectAsStateWithLifecycle()
    val playbackUiState by viewModel.playbackUiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                navController = subNavController,
                title = stringResource(R.string.liked_tracks),
                content = { LikedTracksPlaylistDropDownMenu(onClearTracks = viewModel::clearLikedTracks) }
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        if (!uiState.showAuthError) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(AppPadding.ScaffoldContent)
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val timeInMillis = playlist?.lastEditTime
                        timeInMillis?.let {
                            Text(
                                text = "${stringResource(R.string.last_edited)}: ${convertMillisToDateWithHourAndMinutes(it)}",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                itemsIndexed(playlist?.tracks.orEmpty()) { index, track ->
                    TrackCard(
                        track = track,
                        showPicture = true,
                        playbackUiState = playbackUiState,
                        onTrackClick = { scope.launch { viewModel.setPlaybackQueue(playlist?.tracks.orEmpty(), index) } },
                        onArtistClick = { artistId -> subNavController.navigate(MusicAppRoute.Artist(artistId)) },
                        extraMenu = {
                            SavedTrackDropDownMenu(
                                track = track,
                                onAddToQueue = viewModel::addTrackToQueue,
                                onRemoveTrack = { viewModel.removeTrackFromLikedTracks(track) }
                            )
                        }
                    )
                }
            }
        } else {
            AuthErrorMessage(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(AppPadding.ScaffoldContent)
                    .fillMaxWidth(),
                onClick = {
                    viewModel.logout()
                    mainNavController.navigate(MusicAppRoute.Login) {
                        popUpTo(mainNavController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}
