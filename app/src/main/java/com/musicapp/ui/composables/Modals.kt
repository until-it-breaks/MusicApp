package com.musicapp.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.musicapp.R
import com.musicapp.playback.PlaybackUiState
import com.musicapp.ui.models.TrackModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCreationModal(
    showBottomSheet: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) playlistName = ""
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.new_playlist)) },
                        navigationIcon = {
                            TextButton(onClick = onDismiss) {
                                Text(stringResource(R.string.cancel))
                            }
                        },
                        windowInsets = WindowInsets(0),
                        actions = {
                            TextButton(
                                onClick = {
                                    onCreatePlaylist(playlistName)
                                    onDismiss()
                                },
                                enabled = playlistName.isNotBlank()
                            ) {
                                Text(stringResource(R.string.create))
                            }
                        }
                    )
                }
            ) { contentPadding ->
                Column(
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text(stringResource(R.string.playlist_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlaylistNameModal(
    sheetState: SheetState,
    currentName: String,
    onNameChange: (String) -> Unit,
    canSubmit: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.edit_playlist_name)) },
                    navigationIcon = {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    actions = {
                        TextButton(
                            enabled = canSubmit,
                            onClick = onConfirm
                        ) {
                            Text(stringResource(R.string.change))
                        }
                    },
                    windowInsets = WindowInsets(0)
                )
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(12.dp)
            ) {
                OutlinedTextField(
                    value = currentName,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.new_playlist_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi // Mark as unstable if it uses unstable Media3 APIs
@Composable
fun QueueBottomSheet(
    playbackUiState: PlaybackUiState,
    onDismissRequest: () -> Unit,
    onTrackClick: (TrackModel, Int) -> Unit // Callback when a track in the queue is clicked
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Makes it fully expanded or hidden
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface, // Use surface color for the sheet background
        contentColor = MaterialTheme.colorScheme.onSurface // Use onSurface for text/icons
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxHeight(0.8f)
        ) {
            // Title
            Text(
                text = stringResource(R.string.queue_title), // Define R.string.queue_title
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Playing: track name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.playing_now), // Define R.string.playing_now
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = playbackUiState.currentTrack?.title ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }


            // List of tracks in the queue
            if (playbackUiState.playbackQueue.isNotEmpty()) {
                LazyColumn {
                    itemsIndexed(playbackUiState.playbackQueue) { index, track ->
                        TrackCard(
                            track = track,
                            showPicture = true, // Show picture as in original QueueTrackItem
                            playbackUiState = playbackUiState,
                            onTrackClick = { clickedTrack ->
                                // Pass the track and its index back to the parent
                                onTrackClick(clickedTrack, index)
                            },
                            onArtistClick = { /* Ignored as requested */ },
                            extraMenu = {
                                // Re-add the play button for each item in the queue if desired
                                if (track.id == playbackUiState.currentTrack?.id) {

                                    IconButton(onClick = { onTrackClick(track, index) }) {
                                        Icon(
                                            painter = painterResource(
                                                if (playbackUiState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                                            ),
                                            contentDescription = "Play Track",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp)) // Add spacing between cards
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.empty_queue_message), // Define R.string.empty_queue_message
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}