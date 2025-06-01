package com.musicapp.ui.screens.trackdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.musicapp.R
import com.musicapp.playback.PlaybackUiState
import com.musicapp.ui.models.TrackModel
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar
@UnstableApi
@Composable
fun TrackDetailsScreen(
    trackId: Long,
    navController: NavController,
) {
    val viewModel: TrackDetailsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackUiState by viewModel.playbackUiState.collectAsStateWithLifecycle()

    val currentTrack = remember(uiState, playbackUiState) {
        // Prioritize the currently playing track if it matches the screen's trackId
        if (playbackUiState.currentTrack?.id == trackId) {
            playbackUiState.currentTrack
        } else {
            when (uiState) {
                is TrackDetailsUiState.Success -> (uiState as TrackDetailsUiState.Success).track
                else -> null
            }
        }
    }

    LaunchedEffect(trackId) {
        viewModel.loadTrackDetails(trackId)
    }

    if (currentTrack == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TrackDetailsTopBar(
                track = currentTrack,
                onBackClick = { navController.popBackStack() },
                onMoreClick = { /* TODO */ }
            )
        },
        bottomBar = {
            TrackDetailsPlayerControls(
                playbackUiState = playbackUiState,
                onTogglePlayPause = {
                    if (playbackUiState.currentTrack?.id == currentTrack.id) {
                        viewModel.togglePlayback(currentTrack)
                    } else {
                        viewModel.setPlaybackQueue(
                            listOf(currentTrack),
                            0
                        )
                    }
                },
                onPreviousClick = viewModel::playPreviousTrack,
                onNextClick = viewModel::playNextTrack,
                onSeek = viewModel::seekTo,
                currentPosition = playbackUiState.currentPositionMs,
                trackDuration = playbackUiState.trackDurationMs
            )
        }
    ) { paddingValues ->
        TrackDetailsContent(
            track = currentTrack,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}

@Composable
fun TrackDetailsTopBar(
    track: TrackModel,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Back")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NOW PLAYING FROM ALBUM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
        }
    }
}

@Composable
fun TrackDetailsContent(
    track: TrackModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Or dynamic background based on album art
    ) {
        // Blurred background image (optional, mimicking Spotify)
        Image(
            painter = rememberAsyncImagePainter(
                model = track.bigPictureUri,
                contentScale = ContentScale.Crop,

                //transformations = listOf(BlurTransformation(LocalContext.current, radius = 25f))
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
            //.alpha(0.3f) //alpha for blur effect
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                        startY = 0.5f
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Main Album Art
            Image(
                painter = rememberAsyncImagePainter(model = track.bigPictureUri),
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(300.dp)
                    .aspectRatio(1f)
                    .padding(16.dp)
            )

            // Track Title and Artist
            Text(
                text = track.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Text(
                text = track.contributors.joinToString { it.name },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


@Composable
fun TrackDetailsPlayerControls(
    playbackUiState: PlaybackUiState,
    onTogglePlayPause: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Long) -> Unit,
    currentPosition: Long,
    trackDuration: Long,
    modifier: Modifier = Modifier
) {
    val isPlaying = playbackUiState.isPlaying
    val track = playbackUiState.currentTrack

    if (track == null) {
        Spacer(modifier = Modifier.height(100.dp))
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seek Bar (Progress Bar)
        val progress =
            if (trackDuration > 0) currentPosition.toFloat() / trackDuration.toFloat() else 0f
        var sliderPosition by remember { mutableFloatStateOf(progress) }

        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                sliderPosition = newValue
            },
            onValueChangeFinished = {
                val targetPositionMs = (sliderPosition * playbackUiState.trackDurationMs).toLong()
                onSeek(targetPositionMs)
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Time indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatDuration(currentPosition), style = MaterialTheme.typography.bodySmall)
            Text(text = formatDuration(trackDuration), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    painterResource(R.drawable.ic_shuffle), contentDescription = "Shuffle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Previous
            IconButton(onClick = onPreviousClick) {
                Icon(
                    painterResource(R.drawable.ic_previous), contentDescription = "Previous",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Play/Pause Button
            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Next
            IconButton(onClick = onNextClick) {
                Icon(
                    painterResource(R.drawable.ic_next), contentDescription = "Next",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Repeat
            IconButton(onClick = { /* Handle Repeat */ }) {
                Icon(
                    painterResource(R.drawable.ic_repeat), contentDescription = "Repeat",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}