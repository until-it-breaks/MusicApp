package com.musicapp.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            modifier = Modifier.padding(contentPadding).verticalScroll(scrollState)
        ) {
            if (state.artistDetails == null) {
                Text("Artist Id: ${state.id}")
            }
            state.artistDetails?.let { details ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadableImage(details.mediumPicture, "Artist picture", modifier = Modifier.fillMaxWidth())
                    Text(details.name, style = MaterialTheme.typography.headlineMedium)
                }
            }
            Text("Discography:")
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
                        Text(album.title)
                    }
                }
            }
        }
    }
}