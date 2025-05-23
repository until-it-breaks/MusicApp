package com.musicapp.ui.screens.artist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.PlayListCard
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArtistScreen(navController: NavController, artistId: Long) {
    val viewModel = koinViewModel<ArtistViewModel>()
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(artistId) {
        viewModel.loadArtist(artistId)
        viewModel.loadArtistAlbums(artistId)
    }

    Scaffold(
        topBar = { TopBarWithBackButton(navController, title = stringResource(R.string.artist_details)) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
        ) {
            item {
                uiState.artist?.let { artist ->
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LoadableImage(
                            imageUri = artist.bigPicture,
                            contentDescription = null
                        )
                    }
                }
            }
            item {
                uiState.artist?.let { artist ->
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.discography),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(uiState.artistAlbums){ album ->
                PlayListCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = album.title,
                    imageUri = album.mediumCover,
                    onClick = { navController.navigate(MusicAppRoute.Album(album.id)) }
                )
            }
        }
    }
}