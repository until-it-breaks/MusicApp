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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicapp.data.remote.DeezerDataSource
import com.musicapp.data.remote.DeezerPlaylist
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun HomeContent(modifier: Modifier) {
    val playlists = mutableListOf<DeezerPlaylist>()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(12.dp)
    ) {
        val deezerDataSource = koinInject<DeezerDataSource>()
        val scope = rememberCoroutineScope()
        fun loadMixes() = scope.launch {
            val res = deezerDataSource.getTopPlaylists()
            playlists.addAll(res)
        }
        Button(onClick = ::loadMixes) {
            Text("Load top mixes")
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists) { item ->
                MixItem(
                    title = item.title,
                    onClick = { /*TODO*/ }
                )
            }
        }
    }
}

@Composable
fun MixItem(title: String, image: ImageVector = Icons.Outlined.Image, onClick: () -> Unit) {
    Card(
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = image,
                contentDescription = "Mix picture",
                modifier = Modifier.size(72.dp)
            )
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