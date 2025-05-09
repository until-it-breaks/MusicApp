package com.musicapp.ui.screens.main

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlaylistScreen(navController: NavController, playlistId: Long) {
    val viewModel = koinViewModel<PlaylistViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    Scaffold(
        topBar = { TopBarWithBackButton("Playlist details", navController) }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            if (state.playlistDetailsAreLoading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Text("Error: ${state.error}")
            } else {
                state.playlistDetails?.let { playlist ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LoadableImage(playlist.pictureMedium, "Playlist picture", modifier = Modifier.fillMaxWidth())
                        Text(
                            text = playlist.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (state.tracksAreLoading && state.playlistDetails != null) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                }
            } else {
                state.tracks.forEach { track ->
                    Card(
                        onClick = { Toast.makeText(context, "Playing ${track.title}", Toast.LENGTH_SHORT).show() } // TODO trigger actual music player
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(track.title)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (track.explicitLyrics) {
                                        Icon(Icons.Filled.Explicit, "Explicit")
                                    }
                                    track.contributors.forEachIndexed { index, contributor ->
                                        Text(
                                            text = contributor.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textDecoration = TextDecoration.Underline,
                                            modifier = Modifier
                                                .clickable(onClick = {
                                                    navController.navigate(
                                                        MusicAppRoute.Artist(contributor.id)
                                                    )
                                                })
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
                                Icon(imageVector = Icons.Filled.MoreHoriz, "More")
                            }
                        }
                    }
                }
            }
        }
    }
}