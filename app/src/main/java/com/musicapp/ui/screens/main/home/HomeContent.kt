package com.musicapp.ui.screens.main.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.musicapp.data.remote.DeezerPlaylist
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeContent(modifier: Modifier) {
    val viewModel: HomeViewModel = koinViewModel()
    val playlists: MutableState<List<DeezerPlaylist>> = viewModel.playlists

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(12.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists.value) { item ->
                MixItem(
                    title = item.title,
                    imageUrl = item.pictureSmall,
                    onClick = { /*TODO*/ }
                )
            }
        }
    }
}

@Composable
fun MixItem(title: String, imageUrl: String? = null, onClick: () -> Unit) {
    Card(
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Album picture",
                    modifier = Modifier.size(72.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = "Mix picture",
                    modifier = Modifier.size(72.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}