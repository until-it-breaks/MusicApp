package com.musicapp.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.PersonalTrackDropDownMenu
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TrackCard
import com.musicapp.ui.composables.TrackHistoryDropDownMenu
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
                action = { TrackHistoryDropDownMenu(onClearTracks = viewModel::clearTrackHistory) }
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
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "Playlist image",
                            modifier = Modifier.size(128.dp)
                        )
                        Text(
                            text = "Track history",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                items(playlist.value?.tracks.orEmpty()) { track ->
                    TrackCard(
                        track = track,
                        showPicture = true,
                        onTrackClick = { viewModel.playTrack(track) },
                        onArtistClick = { /*TODO*/ },
                        extraMenu = {
                            PersonalTrackDropDownMenu(
                                trackModel = track,
                                onAddToQueue = { viewModel.addToQueue(track) },
                                onAddToPlaylist = { /*TODO*/ },
                                onRemoveTrack = { viewModel.removeTrackFromTrackHistory(track.id) }
                            )
                        }
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Failed to authenticate. Please login again")
                Button(
                    onClick = {
                        mainNavController.navigate(MusicAppRoute.Login) {
                            popUpTo(mainNavController.graph.id) { inclusive = true }
                        }
                    }
                ) {
                    Text("Go to login")
                }
            }
        }
    }
}
