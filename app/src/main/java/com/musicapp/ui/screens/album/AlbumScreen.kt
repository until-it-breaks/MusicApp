package com.musicapp.ui.screens.album

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TrackCard
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
        topBar = { TopBarWithBackButton(navController, "Album Details") },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
        ) {
            item {
                if (state.error != null) {
                    Text("Error: ${state.error}")
                }
            }
            item {
                state.albumDetails?.let { album ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LoadableImage(
                                imageUri = album.coverBig.toUri(),
                                contentDescription = stringResource(R.string.album_picture_description)
                            )
                        }
                        Text(
                            text = album.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(
                            itemVerticalAlignment = Alignment.CenterVertically,
                        ) {
                            album.contributors.forEach { contributor ->
                                LoadableImage(
                                    imageUri = contributor.smallPicture.toUri(),
                                    contentDescription = "Artist picture",
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            album.contributors.forEachIndexed { index, contributor ->
                                Text(
                                    text = contributor.name,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable(onClick = { navController.navigate(MusicAppRoute.Artist(contributor.id)) })
                                )
                                if (index < album.contributors.lastIndex) {
                                    Text(" · ")
                                }
                            }
                        }
                        Text("Album · ${album.releaseDate}")
                    }
                }
            }
            items(state.tracks) { track ->
                TrackCard(
                    track = track,
                    onTrackClick = { Toast.makeText(context, "Playing ${it.title}", Toast.LENGTH_SHORT).show() }, // TODO trigger actual music player
                    onArtistClick = { artistId -> navController.navigate(MusicAppRoute.Artist(artistId)) },
                    onAddToLiked = viewModel::addToLiked
                )
            }
            item {
                val details = state.albumDetails
                if (details != null) {
                    val durationInMinutes = details.duration / 60
                    Row {
                        Text("${details.trackCount} songs")
                        Text(" · ")
                        Text("${durationInMinutes}min")
                    }
                    Text("Label: ${details.label}")
                }
            }
        }
    }
}