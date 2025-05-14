package com.musicapp.ui.screens.artist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.CenteredCircularProgressIndicator
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri
import com.musicapp.R

@Composable
fun ArtistScreen(navController: NavController, artistId: Long) {
    val viewModel = koinViewModel<ArtistViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(artistId) {
        viewModel.loadArtist(artistId)
        viewModel.loadArtistAlbums(artistId)
    }

    Scaffold(
        topBar = { TopBarWithBackButton(navController, stringResource(R.string.artist_details)) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
        ) {
            item {
                if (state.artistIsLoading) {
                    CenteredCircularProgressIndicator()
                } else if (state.error != null) {
                    Text("Error: ${state.error}")
                }
            }
            item {
                state.artist?.let { artist ->
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LoadableImage(
                            imageUri = artist.bigPicture.toUri(),
                            contentDescription = stringResource(R.string.artist_picture_description),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Albums:",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            item {
                if (state.artistAlbumsAreLoading) {
                    CenteredCircularProgressIndicator()
                }
            }
            items(state.artistAlbums){ album ->
                Card(
                    onClick = { navController.navigate(MusicAppRoute.Album(album.id)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LoadableImage(
                            imageUri = album.mediumCover.toUri(),
                            stringResource(R.string.album_picture_description)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = album.title)
                    }
                }
            }
        }
    }
}