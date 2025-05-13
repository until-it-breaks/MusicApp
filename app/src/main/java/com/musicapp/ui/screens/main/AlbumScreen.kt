package com.musicapp.ui.screens.main

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.CenteredCircularProgressIndicator
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun AlbumScreen(navController: NavController, albumId: Long) {
    val viewModel = koinViewModel<AlbumViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    Scaffold(
        topBar = { TopBarWithBackButton(navController, stringResource(R.string.album_details)) }
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
        ) {
            item {
                if (state.albumDetailsAreLoading) {
                    CircularProgressIndicator()
                } else if (state.error != null) {
                    Text("Error: ${state.error}") // TODO improve message displayed to user
                }
            }
            item {
                state.albumDetails?.let { album ->
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LoadableImage(
                            imageUri = album.coverBig.toUri(),
                            contentDescription = stringResource(R.string.album_picture_description),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        album.contributors.forEachIndexed { index, contributor ->
                            Text(
                                text = contributor.name,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable(onClick = { navController.navigate(MusicAppRoute.Artist(contributor.id)) })
                            )
                            if (index < album.contributors.lastIndex) {
                                Text("·")
                            }
                        }
                    }
                }
            }
            item {
                if (state.tracksAreLoading) {
                    CenteredCircularProgressIndicator()
                }
            }
            items(state.tracks) { track ->
                Card(
                    onClick = { Toast.makeText(context, "Playing ${track.title}", Toast.LENGTH_SHORT).show() } // TODO trigger actual music player
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = track.title)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (track.explicitLyrics) {
                                    Icon(
                                        imageVector = Icons.Filled.Explicit,
                                        contentDescription = stringResource(R.string.explicit_description)
                                    )
                                }
                                track.contributors.forEachIndexed { index, contributor ->
                                    Text(
                                        text = contributor.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier.clickable(onClick = { navController.navigate(MusicAppRoute.Artist(contributor.id)) })
                                    )
                                    if (index < track.contributors.lastIndex) {
                                        Text(
                                            text = ", ",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { /*TODO show additional option*/ }) {
                            Icon(
                                imageVector = Icons.Filled.MoreHoriz,
                                contentDescription = stringResource(R.string.more_description)
                            )
                        }
                    }
                }
            }
            // TODO show more info about album
        }
    }
}