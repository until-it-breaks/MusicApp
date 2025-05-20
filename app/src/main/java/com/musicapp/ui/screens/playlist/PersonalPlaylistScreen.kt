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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.ui.composables.PersonalTrackDropDownMenu
import com.musicapp.ui.composables.TrackCard
import com.musicapp.ui.composables.UserPlaylistTopBar
import com.musicapp.util.convertMillisToDateWithHourAndMinutes
import org.koin.androidx.compose.koinViewModel

@Composable
fun PersonalPlaylistScreen(navController: NavController, playlistId: String) {
    val viewModel = koinViewModel<PersonalPlaylistViewModel>()

    val playlist = viewModel.playlist.collectAsStateWithLifecycle()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistTracks(playlistId)
    }

    Scaffold(
        topBar = {
            UserPlaylistTopBar(
                navController = navController,
                title = "",
                onAddTrack = { /*TODO*/ },
                onEditName = { /*TODO*/ },
                onDeletePlaylist = viewModel::deletePlaylist
            )
         },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
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
                    Text(playlist.value?.name ?: "Unknown playlist")
                    val timeInMillis = playlist.value?.lastEditTime
                    timeInMillis?.let {
                        Text("Last edited: ${convertMillisToDateWithHourAndMinutes(it)}")
                    }
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
                            onRemoveTrack = { viewModel.removeTrackFromPlaylist(track.id) }
                        )
                    }
                )
            }
        }
    }
}
