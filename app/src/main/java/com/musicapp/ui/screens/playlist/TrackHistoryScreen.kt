package com.musicapp.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TrackCard
import com.musicapp.ui.models.TrackModel

@Composable
fun TrackHistoryScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopBarWithBackButton(
                navController,
                "Track history"
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
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
            var songs = listOf("Song1", "Song2", "Song3")
            items(songs) { song ->
                TrackCard(
                    TrackModel(1, song),
                    onTrackClick = { /**/ },
                    onArtistClick = { /**/ }
                ) { }
            }
        }
    }
}
