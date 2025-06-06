package com.musicapp.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.QueuePlayNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import com.musicapp.R
import com.musicapp.playback.QueueItem
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.screens.addtoplaylist.AddTrackToPlaylistModal

/**
 * Drop-down menu for user playlists
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPlaylistDropDownMenu(
    onDeletePlaylist: () -> Unit,
    onEditPlaylistName: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = !expanded }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit_playlist_name)) },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = {
                    expanded = !expanded
                    onEditPlaylistName()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete_playlist)) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = {
                    expanded = !expanded
                    showConfirmDialog = true
                }
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = stringResource(R.string.confirm_delete_playlist)) },
            text = { Text(text = stringResource(R.string.confirm_delete_playlist_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onDeletePlaylist()
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Drop-down menu for tracks retrieved via deezer API
 */
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicTrackDropDownMenu(
    trackModel: TrackModel,
    onAddToQueue: (track: TrackModel) -> Unit,
    onLiked: (track: TrackModel) -> Unit,
    modifier: Modifier = Modifier,
    onRemoveFromQueue: (queueItem: QueueItem) -> Unit = {},
    queueItem: QueueItem? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = !expanded }
        ) {

            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_to_queue)) },
                leadingIcon = { Icon(Icons.Outlined.QueuePlayNext, contentDescription = null)},
                onClick = {
                    expanded = !expanded
                    onAddToQueue(trackModel)
                }
            )
            if (queueItem != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.remove_from_queue)) },
                    leadingIcon = { Icon(Icons.Outlined.QueuePlayNext, contentDescription = null)},
                    onClick = {
                        expanded = !expanded
                        onRemoveFromQueue(queueItem)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_to_liked)) },
                leadingIcon = { Icon(Icons.Outlined.Favorite, contentDescription = null)},
                onClick = {
                    expanded = !expanded
                    onLiked(trackModel)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_to_playlist)) },
                leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null)},
                onClick = {
                    showBottomSheet = !showBottomSheet
                    expanded = !expanded
                },
            )
        }
    }
    AddTrackToPlaylistModal(
        track = trackModel,
        showBottomSheet = showBottomSheet,
        sheetState = sheetState,
        onDismiss = { showBottomSheet = !showBottomSheet }
    )
}

/**
 * Drop-down menu used for tracks that the user has saved
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedTrackDropDownMenu(
    track: TrackModel,
    onAddToQueue: (track: TrackModel) -> Unit,
    onRemoveTrack: (track: TrackModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_to_queue)) },
                leadingIcon = { Icon(Icons.Outlined.QueuePlayNext, contentDescription = null)},
                onClick = {
                    expanded = !expanded
                    onAddToQueue(track)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_to_playlist)) },
                leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null)},
                onClick = {
                    showBottomSheet = !showBottomSheet
                    expanded = !expanded
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.remove_track)) },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null)},
                onClick = {
                    expanded = !expanded
                    onRemoveTrack(track)
                }
            )
        }
    }
    AddTrackToPlaylistModal(
        track = track,
        showBottomSheet = showBottomSheet,
        sheetState = sheetState,
        onDismiss = { showBottomSheet = !showBottomSheet }
    )
}

/**
 * Drop-down menu for liked tracks playlist
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedTracksPlaylistDropDownMenu(modifier: Modifier = Modifier, onClearTracks: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.clear_tracks)) },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null)},
                onClick = {
                    expanded = !expanded
                    showConfirmDialog = true
                }
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = stringResource(R.string.confirm_clear_liked_tracks)) },
            text = { Text(text = stringResource(R.string.confirm_clear_liked_tracks_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onClearTracks()
                    }
                ) {
                    Text(text = stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Drop-down menu for track history
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackHistoryDropDownMenu(modifier: Modifier = Modifier, onClearTracks: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.clear_tracks)) },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null)},
                onClick = {
                    expanded = !expanded
                    showConfirmDialog = true
                }
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = stringResource(R.string.confirm_clear_history)) },
            text = { Text(text = stringResource(R.string.confirm_clear_history_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onClearTracks()
                    }
                ) {
                    Text(text = stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}