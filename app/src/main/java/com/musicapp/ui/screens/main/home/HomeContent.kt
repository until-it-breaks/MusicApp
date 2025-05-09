package com.musicapp.ui.screens.main.home

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.CenteredCircularProgressIndicator
import com.musicapp.ui.composables.LoadableImage
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(navController: NavController, modifier: Modifier) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = viewModel::loadContent,
        state = pullToRefreshState
    ) {
        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.padding(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.top_playlists),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                if (state.isLoading) {
                    CenteredCircularProgressIndicator()
                }
            }
            items(state.playlists.chunked(2)) { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { playlist ->
                        PlayListCard(
                            title = playlist.title,
                            imageUri = playlist.mediumPicture.toUri(),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(MusicAppRoute.Playlist(playlist.id)) }
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.top_artists),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                if (state.isLoading) {
                    CenteredCircularProgressIndicator()
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.artists) { artist ->
                        ArtistCard(
                            title = artist.name,
                            imageUrl = artist.mediumPicture,
                            onClick = { navController.navigate(MusicAppRoute.Artist(artist.id)) }
                        )
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.top_albums),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                if (state.isLoading) {
                    CenteredCircularProgressIndicator()
                }
            }
            items(state.albums.chunked(2)) { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        PlayListCard(
                            title = item.title,
                            imageUri = item.mediumCover.toUri(),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(MusicAppRoute.Album(item.id)) }
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun PlayListCard(modifier: Modifier = Modifier, title: String, imageUri: Uri? = null, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoadableImage(
                imageUri = imageUri,
                contentDescription = stringResource(R.string.playlist_picture_description),
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun ArtistCard(modifier: Modifier = Modifier, title: String, imageUrl: String? = null, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.width(96.dp)
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            LoadableImage(
                imageUri = imageUrl?.toUri(),
                contentDescription = stringResource(R.string.artist_picture_description),
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}