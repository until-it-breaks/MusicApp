package com.musicapp.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.AuthErrorMessage
import com.musicapp.ui.composables.SavedTrackDropDownMenu
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TrackCard
import com.musicapp.ui.composables.TrackHistoryDropDownMenu
import com.musicapp.util.convertMillisToDateWithHourAndMinutes
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackHistoryScreen(mainNavController: NavController, subNavController: NavController) {
    val viewModel = koinViewModel<TrackHistoryViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playlist = viewModel.playlist.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                navController = subNavController,
                content = { TrackHistoryDropDownMenu(onClearTracks = viewModel::clearTrackHistory) }
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        if (!uiState.showAuthError) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(12.dp)
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.track_history),
                            style = MaterialTheme.typography.titleLarge
                        )
                        val timeInMillis = playlist.value?.lastEditTime
                        timeInMillis?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${stringResource(R.string.last_edited)}: ${convertMillisToDateWithHourAndMinutes(it)}")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                items(playlist.value?.tracks.orEmpty()) { track ->
                    TrackCard(
                        track = track,
                        showPicture = true,
                        onTrackClick = { viewModel.playTrack(track) },
                        onArtistClick = { artistId -> subNavController.navigate(MusicAppRoute.Artist(artistId)) },
                        extraMenu = {
                            SavedTrackDropDownMenu(
                                trackModel = track,
                                onAddToQueue = { viewModel.addToQueue(track) },
                                onRemoveTrack = { viewModel.removeTrackFromTrackHistory(track.id) }
                            )
                        }
                    )
                }
            }
        } else {
            AuthErrorMessage(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(12.dp)
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
