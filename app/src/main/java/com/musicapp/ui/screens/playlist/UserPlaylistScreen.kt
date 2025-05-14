package com.musicapp.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.musicapp.ui.composables.UserPlaylistTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserPlaylistScreen(navController: NavController, playlistId: String) {
    val viewModel = koinViewModel<UserPlaylistViewModel>()

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
        }
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
        ) {
            item {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = "Playlist image"
                )
            }
            item {
                Text("Playlist name")
            }
            var songs = listOf("Song1", "Song2", "Song3")
            items(songs) {
                Card(onClick = { /*TODO*/ }) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "Track image"
                        )
                        Text(it)
                    }
                }
            }
        }
    }
}
