package com.musicapp.ui.screens.album

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.CenteredCircularProgressIndicator
import com.musicapp.ui.composables.CenteredLinearProgressIndicator
import com.musicapp.ui.composables.ErrorSection
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.PublicTrackDropDownMenu
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TrackCard
import com.musicapp.ui.theme.AppPadding
import com.musicapp.util.convertDurationInSecondsToString
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(navController: NavController, albumId: Long) {
    val viewModel = koinViewModel<AlbumViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackUiState by viewModel.playbackUiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                navController,
                title = stringResource(R.string.album_details)
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(AppPadding.ScaffoldContent)
        ) {
            item {
                if (uiState.showAlbumDetailsLoading) {
                    CenteredCircularProgressIndicator()
                }
                uiState.albumErrorStringId?.let {
                    ErrorSection(
                        title = stringResource(R.string.failed_to_load_album),
                        message = stringResource(it),
                        onRetry = { viewModel.loadAlbum(albumId) }
                    )
                }
                uiState.albumDetails?.let { album ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LoadableImage(
                                imageUri = album.bigCoverUri,
                                contentDescription = null
                            )
                        }
                        Text(
                            text = album.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width((36 + (album.contributors.size - 1) * 24).dp)
                                    .height(36.dp)
                            ) {
                                album.contributors.forEachIndexed { index, contributor ->
                                    LoadableImage(
                                        imageUri = contributor.smallPictureUri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .offset(x = (index * 24).dp)
                                            .zIndex((album.contributors.size - index).toFloat())
                                            .clip(CircleShape)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FlowRow(
                                itemVerticalAlignment = Alignment.CenterVertically,
                            ) {
                                album.contributors.forEachIndexed { index, contributor ->
                                    Text(
                                        text = contributor.name,
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier.clickable {
                                            navController.navigate(
                                                MusicAppRoute.Artist(contributor.id)
                                            )
                                        }
                                    )
                                    if (index < album.contributors.lastIndex) {
                                        Text(" · ")
                                    }
                                }
                            }
                        }
                        Text("Album · ${LocalDate.parse(album.releaseDate).year}")
                    }
                }
            }
            item {
                if (uiState.showTracksLoading) {
                    CenteredLinearProgressIndicator()
                }
                uiState.tracksErrorStringId?.let {
                    ErrorSection(
                        title = stringResource(R.string.failed_to_load_tracks),
                        message = stringResource(it) + if (uiState.failedTracksCount > 0) " (${uiState.failedTracksCount} ${stringResource(R.string.tracks)})" else "",
                        onRetry = viewModel::loadTracks
                    )
                }
            }
            itemsIndexed(uiState.tracks) { index, track ->
                TrackCard(
                    track = track,
                    playbackUiState = playbackUiState,
                    onTrackClick = {
                        scope.launch {
                            viewModel.setPlaybackQueue(
                                uiState.tracks,
                                index
                            )
                        }
                    },
                    onArtistClick = { artistId ->
                        navController.navigate(
                            MusicAppRoute.Artist(
                                artistId
                            )
                        )
                    },
                    extraMenu = {
                        PublicTrackDropDownMenu(
                            trackModel = track,
                            onAddToQueue = viewModel::addTrackToQueue,
                            onLiked = viewModel::addToLiked
                        )
                    },
                )
            }
            item {
                uiState.albumDetails?.let {
                    Row {
                        Text("${it.trackCount} ${stringResource(R.string.tracks)}")
                        it.duration?.let {
                            Text(" · ")
                            Text(convertDurationInSecondsToString(it))
                        }
                    }
                    Text("${stringResource(R.string.album_label)}: ${it.label}")
                }
            }
        }
    }
}