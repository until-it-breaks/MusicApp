package com.musicapp.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explicit
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun AlbumScreen(navController: NavController, albumId: Long) {
    val viewModel = koinViewModel<AlbumViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    Scaffold(
        topBar = { TopBarWithBackButton("Album details", navController) }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding).verticalScroll(scrollState)
        ) {
            if (state.albumDetails == null) {
                Text("Nothing to show")
            }
            state.albumDetails?.let { details ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadableImage(details.coverMedium, "Album picture", modifier = Modifier.fillMaxWidth())
                    Text(details.title, style = MaterialTheme.typography.headlineMedium)
                }
            }
            state.trackDetails.forEach { track ->
                Card {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(track.title)
                            Row {
                                if (track.explicitLyrics) {
                                    Icon(Icons.Filled.Explicit, "Explicit")
                                }
                                track.contributors.forEachIndexed { index, contributor ->
                                    Text(
                                        text = contributor.name,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier
                                            .clickable(
                                                onClick = { navController.navigate(MusicAppRoute.Artist(contributor.id)) }
                                            )
                                    )
                                    if (index < track.contributors.lastIndex) {
                                        Text(", ")
                                    }
                                }
                            }
                        }
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