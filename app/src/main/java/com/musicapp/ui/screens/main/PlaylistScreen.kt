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
fun PlaylistScreen(navController: NavController, playlistId: Long) {
    val viewModel = koinViewModel<PlaylistViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    Scaffold(
        topBar = { TopBarWithBackButton(navController, stringResource(R.string.playlist_details)) }
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
        ) {
            item {
                if (state.playlistDetailsAreLoading) {
                    CenteredCircularProgressIndicator()
                } else if (state.error != null) {
                    Text("Error: ${state.error}")
                }
            }
            item {
                state.playlistDetails?.let { playlist ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LoadableImage(
                            imageUri = playlist.pictureBig.toUri(),
                            stringResource(R.string.playlist_picture_description),
                            modifier = Modifier.fillMaxWidth())
                        Text(
                            text = playlist.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
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
                                        stringResource(R.string.explicit_description)
                                    )
                                }
                                track.contributors.forEachIndexed { index, contributor ->
                                    Text(
                                        text = contributor.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier
                                            .clickable(onClick = { navController.navigate(MusicAppRoute.Artist(contributor.id)) })
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
                            Icon(imageVector = Icons.Filled.MoreHoriz, stringResource(R.string.more_description))
                        }
                    }
                }
            }
        }
    }
}