package com.musicapp.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArtistScreen(navController: NavController, artistId: Long) {
    val viewModel = koinViewModel<ArtistViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(artistId) {
        viewModel.loadArtist(artistId)
        viewModel.loadArtistAlbums(artistId)
    }

    Scaffold(
        topBar = { TopBarWithBackButton("Artist details", navController) }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            if (state.artistIsLoading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Text("Error: ${state.error}")
            } else {
                state.artist?.let { details ->
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LoadableImage(details.bigPicture, "Artist picture", modifier = Modifier.fillMaxWidth())
                    }
                    Text(
                        text = details.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Albums:",
                    style = MaterialTheme.typography.titleMedium
                )
                if (state.artistAlbumsAreLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    state.artistAlbums.forEach { album ->
                        Card(
                            onClick = { navController.navigate(MusicAppRoute.Album(album.id)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LoadableImage(album.mediumCover, "Album picture")
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(album.title)
                            }
                        }
                    }
                }
            }
        }
    }
}