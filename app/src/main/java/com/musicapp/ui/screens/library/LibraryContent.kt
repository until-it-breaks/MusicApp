package com.musicapp.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LibraryContent() {
    val playlists = (1..8).map { "Playlist #$it" }
    Column {
        Text("Your playlists")
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistItem(
                    playlist,
                    onClick = { /*TODO*/ }
                )
            }
        }
    }
}

@Composable
fun PlaylistItem(item: String, onClick: () -> Unit) {
    Card(
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                Icons.Outlined.Image,
                "Item Picture",
                modifier = Modifier.size(48.dp)
                    .padding(end = 8.dp)
            )
            Text(item)
        }
    }
}