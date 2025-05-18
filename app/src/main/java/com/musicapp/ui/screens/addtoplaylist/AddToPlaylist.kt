package com.musicapp.ui.screens.addtoplaylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicapp.ui.composables.AddTrackToPlaylistPlaylistCard
import com.musicapp.ui.composables.AddTrackToPlaylistTrackCard
import com.musicapp.ui.models.TrackModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTrackToPlaylistModal(
    track: TrackModel,
    showBottomSheet: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<AddToPlaylistViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val clickedPlaylists = remember { mutableStateOf(setOf<String>()) }
    var addToLiked by remember { mutableStateOf(false) }
    var isInLiked by remember { mutableStateOf(false) }

    LaunchedEffect(track) {
        isInLiked = viewModel.isInLiked(track)
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = null
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Add To Playlist") },
                        navigationIcon = {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                        },
                        windowInsets = WindowInsets(0)
                    )
                }
            ) { contentPadding ->
                Column(
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.showAuthError) {
                        Text("Login again") // TODO
                    } else {
                        AddTrackToPlaylistTrackCard(track)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (uiState.likedTracksPlaylist != null) {
                                AddTrackToPlaylistPlaylistCard(
                                    title = "Liked tracks",
                                    onClick = { addToLiked = !addToLiked },
                                    enabled = !isInLiked,
                                    showCheck = addToLiked
                                )
                            }
                            for (playlist in uiState.playlists) {
                                var isInPlaylist by remember(playlist.id) { mutableStateOf(false) }

                                LaunchedEffect(playlist.id, track) {
                                    isInPlaylist = viewModel.isInPlaylist(playlist.id, track)
                                }

                                AddTrackToPlaylistPlaylistCard(
                                    title = playlist.name,
                                    onClick = {
                                        clickedPlaylists.value = clickedPlaylists.value.toMutableSet().apply {
                                            if (contains(playlist.id)) {
                                                remove(playlist.id)
                                            } else {
                                                add(playlist.id)
                                            }
                                        }
                                    },
                                    enabled = !isInPlaylist,
                                    showCheck = clickedPlaylists.value.contains(playlist.id)
                                )
                            }
                        }
                        if (addToLiked || !clickedPlaylists.value.isEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(onClick = {
                                    if (addToLiked) {
                                        viewModel.addToLiked(track)
                                    }
                                    viewModel.addToPlaylists(track, clickedPlaylists.value)
                                    onDismiss()
                                }) {
                                    Text("Done")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}