package com.musicapp.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.navigation.NavController
import com.musicapp.ui.composables.UserPlaylistTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun PersonalPlaylistScreen(navController: NavController, playlistId: String) {
    val viewModel = koinViewModel<PersonalPlaylistViewModel>()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistTracks(playlistId)
    }

    Scaffold(
        topBar = { UserPlaylistTopBar(
            navController,
            "Personal playlist",
            onAddTrack = {},
            onEditName = {},
            onDeletePlaylist = { viewModel.deletePlaylist() })
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
                    Text("Playlist name")
                }
            }
        }
    }
}
