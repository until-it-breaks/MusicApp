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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import com.musicapp.data.remote.deezer.DeezerTrackDetailed

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
            onDismissRequest = { expanded = false }
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

@Composable
fun TrackDropdownMenu(deezerTrackDetailed: DeezerTrackDetailed, onAddToLiked: (track: DeezerTrackDetailed) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Add to liked") },
                onClick = { onAddToLiked(deezerTrackDetailed) }
            )
            DropdownMenuItem(
                text = { Text("Add to playlist") },
                onClick = { /*TODO*/ }
            )
        }
    }
}