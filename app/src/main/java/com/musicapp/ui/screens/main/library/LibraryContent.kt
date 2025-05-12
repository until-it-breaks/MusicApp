package com.musicapp.ui.screens.main.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicapp.ui.composables.CreatePlaylistModal
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(modifier: Modifier) {
    val viewModel = koinViewModel<LibraryViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.padding(12.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                state.likedTracksPlaylist?.let { likedPlaylist ->
                    PlaylistItem(
                        "Liked songs",
                        onClick = { /*TODO*/ }
                    )
                }
            }
            item {
                state.trackHistory?.let { history ->
                    PlaylistItem(
                        "Track history",
                        onClick = { /*TODO*/ }
                    )
                }
            }
            item {
                Text(
                    text = "Your playlists",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            if (state.playlists.isEmpty()) {
                item {
                    Text("No playlists yet!")
                }
            } else {
                items(state.playlists) { playlist ->
                    PlaylistItem(
                        playlist.name,
                        onClick = { /*TODO*/ }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { showBottomSheet = true},
            modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Add, "Create new playlist")
        }
    }
    CreatePlaylistModal(
        showBottomSheet = showBottomSheet,
        sheetState = sheetState,
        onDismiss = { showBottomSheet = false },
        onCreatePlaylist = { /* call viewmodel method to create playlist */ }
    )
}

@Composable
fun PlaylistItem(title: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = "Playlist Picture",
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = "Forward",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}