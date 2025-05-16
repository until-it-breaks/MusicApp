package com.musicapp.ui.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.QueuePlayNext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.musicapp.ui.models.TrackModel

/*TODO a lot*/
@Composable
fun UserPlaylistDropDownMenu(onAddTrack: () -> Unit, onEditName: () -> Unit, onDeletePlaylist: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = !expanded }
        ) {
            DropdownMenuItem(
                text = { Text("Add song") },
                leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = onAddTrack
            )
            DropdownMenuItem(
                text = { Text("Edit name") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = onEditName
            )
            DropdownMenuItem(
                text = { Text("Delete playlist") },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = onDeletePlaylist
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicTrackDropDownMenu(
    trackModel: TrackModel,
    modifier: Modifier = Modifier,
    onLiked: (track: TrackModel) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = !expanded }
        ) {
            DropdownMenuItem(
                text = { Text("Add to queue") },
                leadingIcon = { Icon(Icons.Outlined.QueuePlayNext, contentDescription = null)},
                onClick = {
                    /*TODO call to the music player service */
                    expanded = !expanded
                }
            )
            DropdownMenuItem(
                text = { Text("Add to Liked") },
                leadingIcon = { Icon(Icons.Outlined.Favorite, contentDescription = null)},
                onClick = {
                    onLiked(trackModel)
                    expanded = !expanded
                }
            )
            DropdownMenuItem(
                text = { Text("Add to playlist") },
                leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null)},
                onClick = {
                    showBottomSheet = !showBottomSheet
                    expanded = !expanded
                },
            )
        }
    }
    AddTrackToPlaylistModal(
        showBottomSheet,
        sheetState,
        onDismiss = {
            showBottomSheet = !showBottomSheet
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalTrackDropDownMenu(trackModel: TrackModel, modifier: Modifier = Modifier) {

    var expanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Add to queue") },
                leadingIcon = { Icon(Icons.Outlined.QueuePlayNext, contentDescription = null)},
                onClick = {
                    /*TODO call to the music player service */
                    expanded = !expanded
                }
            )
            DropdownMenuItem(
                text = { Text("Add to playlist") },
                leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null)},
                onClick = {
                    showBottomSheet = !showBottomSheet
                    expanded = !expanded
                }
            )
            DropdownMenuItem(
                text = { Text("Remove track") },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null)},
                onClick = { /*TODO*/ }
            )
        }
    }
    AddTrackToPlaylistModal(
        showBottomSheet,
        sheetState,
        onDismiss = {
            showBottomSheet = !showBottomSheet
        }
    )
}