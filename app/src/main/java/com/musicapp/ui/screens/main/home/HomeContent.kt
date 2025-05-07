package com.musicapp.ui.screens.main.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.LoadableImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(navController: NavController, modifier: Modifier) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()
    val scrollState = rememberScrollState()

    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
        state = pullToRefreshState
    ) {
        Column (
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
                .padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = stringResource(R.string.top_playlists),
                style = MaterialTheme.typography.titleLarge
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(state.playlists) { item ->
                    PlayListCard(
                        title = item.title,
                        imageUrl = item.mediumPicture,
                        onClick = { /*TODO*/ }
                    )
                }
            }
            Text(
                text = stringResource(R.string.top_artists),
                style = MaterialTheme.typography.titleLarge
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(150.dp)
            ) {
                items(state.artists) { item ->
                    ArtistCard(
                        title = item.name,
                        imageUrl = item.mediumPicture,
                        onClick = { /*TODO*/ }
                    )
                }
            }
            Text(
                text = stringResource(R.string.top_albums),
                style = MaterialTheme.typography.titleLarge
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(state.albums) { item ->
                    PlayListCard(
                        title = item.title,
                        imageUrl = item.mediumCover,
                        onClick = { navController.navigate(MusicAppRoute.Album(item.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlayListCard(title: String, imageUrl: String? = null, onClick: () -> Unit) {
    Card(
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoadableImage(
                imageUrl = imageUrl,
                contentDescription = stringResource(R.string.playlist_picture_description),
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun ArtistCard(title: String, imageUrl: String? = null, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 96.dp, height = 120.dp)
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            LoadableImage(
                imageUrl = imageUrl,
                contentDescription = stringResource(R.string.artist_picture_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}