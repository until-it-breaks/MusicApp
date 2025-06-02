package com.musicapp.ui.screens.trackdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
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
import com.musicapp.ui.composables.MainTopBar
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TopBarWithBackButtonAndMoreVert
import java.util.Locale

enum class RepeatMode {
    OFF,
    ON,
    ONE,
    ONCE
}

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar
@UnstableApi
@Composable
fun TrackDetailsScreen(
    trackId: Long,
    navController: NavController,
) {
    val viewModel: TrackDetailsViewModel = koinViewModel()
    val playbackUiState by viewModel.playbackUiState.collectAsStateWithLifecycle()

    val currentTrack: TrackModel? by remember(playbackUiState.currentTrack, trackId) {
        derivedStateOf {
            if (playbackUiState.currentTrack?.id == trackId) {
                playbackUiState.currentTrack
            } else {
                null
            }
        }
    }

    LaunchedEffect(trackId) {
        // viewModel.loadTrackDetails(trackId)
    }

    if (currentTrack == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopBarWithBackButtonAndMoreVert(
                navController = navController,
                title = stringResource(R.string.trackDetails),
                onMoreVertClick = { /*TODO*/ })
        },
        bottomBar = {
            TrackDetailsPlayerControls(
                playbackUiState = playbackUiState,
                onTogglePlayPause = {
                    if (playbackUiState.currentTrack?.id == currentTrack?.id) {
                        viewModel.togglePlayback(currentTrack!!)
                    } else {
                        viewModel.setPlaybackQueue(
                            listOf(currentTrack!!),
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
            track = currentTrack!!,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
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


// bottom bar with controls buttons
@OptIn(ExperimentalMaterial3Api::class)
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

    var isShuffleOn by remember { mutableStateOf(false) }
    var currentRepeatMode by remember { mutableStateOf(RepeatMode.OFF) }

    if (track == null) {
        Spacer(modifier = Modifier.height(100.dp))
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(8.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seek Bar (Progress Bar)
        val playbackProgress = remember(playbackUiState.currentPositionMs, playbackUiState.trackDurationMs) {
            if (playbackUiState.trackDurationMs > 0)
                playbackUiState.currentPositionMs.toFloat() / playbackUiState.trackDurationMs.toFloat()
            else 0f
        }
        var sliderPosition by remember { mutableFloatStateOf(playbackProgress) }
        var isDragging by remember { mutableStateOf(false) }
        LaunchedEffect(playbackProgress, isDragging) {
            if (!isDragging) {
                sliderPosition = playbackProgress
            }
        }

        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                isDragging = true
                sliderPosition = newValue
            },
            onValueChangeFinished = {
                isDragging = false
                val targetPositionMs = (sliderPosition * playbackUiState.trackDurationMs).toLong()
                onSeek(targetPositionMs)
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth(),
            thumb = {
                Box(
                    modifier = Modifier
                        .offset(y = 2.dp)
                        .size(12.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(4.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )
            }
        )

        // Time indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatDuration(currentPosition), style = MaterialTheme.typography.bodySmall)
            Text(text = formatDuration(trackDuration), style = MaterialTheme.typography.bodySmall)
        }

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = {
                isShuffleOn = !isShuffleOn
                // TODO Handle shuffle
            }) {
                Icon(
                    painterResource(if (isShuffleOn) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle),
                    contentDescription = "Shuffle",
                    tint = if (isShuffleOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Previous
            IconButton(onClick = onPreviousClick) {
                Icon(
                    painterResource(R.drawable.ic_skip_previous), contentDescription = "Previous",
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
                    painterResource(R.drawable.ic_skip_next), contentDescription = "Next",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Repeat
            IconButton(onClick = {
                currentRepeatMode = when (currentRepeatMode) {
                    RepeatMode.OFF -> RepeatMode.ON
                    RepeatMode.ON -> RepeatMode.ONE
                    RepeatMode.ONE -> RepeatMode.ONCE
                    RepeatMode.ONCE -> RepeatMode.OFF
                }
                // TODO Handle repeat
            }) {
                val iconResource = when (currentRepeatMode) {
                    RepeatMode.OFF -> R.drawable.ic_repeat
                    RepeatMode.ON -> R.drawable.ic_repeat_on
                    RepeatMode.ONE -> R.drawable.ic_repeat_one_on
                    RepeatMode.ONCE -> R.drawable.ic_play_one
                }
                val iconTint =
                    if (currentRepeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                Icon(
                    painterResource(iconResource),
                    contentDescription = "Repeat",
                    tint = iconTint
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