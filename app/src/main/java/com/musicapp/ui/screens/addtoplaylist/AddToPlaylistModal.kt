package com.musicapp.ui.screens.addtoplaylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicapp.R
import com.musicapp.ui.composables.PlaylistCardToAddTo
import com.musicapp.ui.composables.TrackCardToAdd
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

    val playlists = viewModel.playlists.collectAsStateWithLifecycle()
    val likedPlaylist = viewModel.likedPlaylist.collectAsStateWithLifecycle()

    val clickedPlaylists = remember { mutableStateOf(setOf<String>()) }
    var addToLiked by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.add_to_playlist)) },
                        navigationIcon = {
                            TextButton(onClick = onDismiss) {
                                Text(stringResource(R.string.cancel))
                            }
                        },
                        actions = {
                            TextButton(
                                onClick = {
                                    if (addToLiked) {
                                        viewModel.addToLiked(track)
                                    }
                                    viewModel.addToPlaylists(track, clickedPlaylists.value)
                                    onDismiss()
                                }
                            ) {
                                Text(stringResource(R.string.add))
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
                    TrackCardToAdd(track)
                    Text(stringResource(R.string.select_playlists_to_add_track_to))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PlaylistCardToAddTo(
                            title = stringResource(R.string.liked_tracks),
                            onClick = { addToLiked = !addToLiked },
                            enabled = likedPlaylist.value?.tracks?.none { it.id == track.id } == true,
                            showCheck = addToLiked
                        )
                        for (playlist in playlists.value) {
                            PlaylistCardToAddTo(
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
                                enabled = playlist.tracks.none { it.id == track.id } == true,
                                showCheck = clickedPlaylists.value.contains(playlist.id)
                            )
                        }
                    }
                }
            }
        }
    }
}