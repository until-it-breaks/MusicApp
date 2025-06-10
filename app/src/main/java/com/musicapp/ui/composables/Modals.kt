package com.musicapp.ui.composables

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.musicapp.playback.BasePlaybackViewModel
import com.musicapp.playback.PlaybackUiState
import com.musicapp.playback.QueueItem
import com.musicapp.data.models.TrackModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
@UnstableApi
@Composable
fun QueueBottomSheet(
    playbackUiState: PlaybackUiState,
    onClearQueueClicked: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxHeight(0.8f)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                // Title
                Text(
                    text = stringResource(R.string.queue_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // clear queue button
                TextButton(onClick = { onClearQueueClicked() }) {
                    Text(stringResource(R.string.clear_queue))
                }
            }

            // Playing: track name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.playing_now),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = playbackUiState.currentQueueItem?.track?.title ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // List of tracks in the queue
            if (playbackUiState.playbackQueue.isNotEmpty()) {
                val viewModel: BasePlaybackViewModel = koinViewModel()
                LazyColumn {
                    itemsIndexed(playbackUiState.playbackQueue) { index, queueItem ->
                        val track = queueItem.track
                        TrackCard(
                            track = track,
                            showPicture = true,
                            queueId = queueItem.id,
                            playbackUiState = playbackUiState,
                            onTrackClick = { clickedTrack ->
                                scope.launch {
                                    onTrackClick(
                                        queueItem,
                                        playbackUiState,
                                        viewModel,
                                        clickedTrack,
                                        index
                                    )
                                }
                            },
                            onArtistClick = { },
                            extraMenu = {
                                if (queueItem.id == playbackUiState.currentQueueItemId) {
                                    IconButton(onClick = {
                                        scope.launch {
                                            onTrackClick(
                                                queueItem,
                                                playbackUiState,
                                                viewModel,
                                                queueItem.track,
                                                index
                                            )
                                        }
                                    }) {
                                        Icon(
                                            painter = painterResource(
                                                if (playbackUiState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                                            ),
                                            contentDescription = "Play Track",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    PublicTrackDropDownMenu(
                                        trackModel = track,
                                        onLiked = { /*TODO*/ },
                                        onAddToQueue = viewModel::addTrackToQueue,
                                        onRemoveFromQueue = viewModel::removeTrackFromQueue,
                                        queueItem = queueItem
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.empty_queue_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
private suspend fun onTrackClick(
    queueItem: QueueItem,
    playbackUiState: PlaybackUiState,
    viewModel: BasePlaybackViewModel,
    clickedTrack: TrackModel,
    index: Int
) {
    if (queueItem.id == playbackUiState.currentQueueItemId) {
        viewModel.togglePlayback(clickedTrack)
    } else {
        val trackQueue = playbackUiState.playbackQueue.map { queueItem ->
            queueItem.track
        }
        viewModel.setPlaybackQueue(trackQueue, index)
    }
}