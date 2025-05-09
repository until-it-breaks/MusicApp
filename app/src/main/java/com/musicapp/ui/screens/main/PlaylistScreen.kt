package com.musicapp.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlaylistScreen(navController: NavController, playlistId: Long) {
    val viewModel = koinViewModel<PlaylistViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    Scaffold(
        topBar = { TopBarWithBackButton("Playlist details", navController) }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding).verticalScroll(scrollState)
        ) {
            if (state.details == null) {
                Text("${state.id}")
            }
            state.details?.let { details ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadableImage(details.pictureMedium, "Playlist picture", modifier = Modifier.fillMaxWidth())
                    Text(details.title, style = MaterialTheme.typography.headlineMedium)
                }
            }
            state.details?.tracks?.tracks?.forEach { track ->
                Card {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(track.title)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${track.duration}s")
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(imageVector = Icons.Filled.PlayArrow, "Play song")
                        }
                    }
                }
            }
        }
    }
}