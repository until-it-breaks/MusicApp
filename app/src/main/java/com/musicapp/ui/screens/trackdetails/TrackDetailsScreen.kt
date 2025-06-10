package com.musicapp.ui.screens.trackdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.musicapp.R
import com.musicapp.data.models.TrackModel
import com.musicapp.playback.PlaybackUiState
import com.musicapp.playback.RepeatMode
import com.musicapp.ui.composables.PublicTrackDropDownMenu
import com.musicapp.ui.composables.QueueBottomSheet
import com.musicapp.ui.composables.TopBarWithBackButton
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun TrackDetailsScreen(
    navController: NavController,
) {
    val viewModel = koinViewModel<TrackDetailsViewModel>()
    val playbackUiState by viewModel.playbackUiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val currentTrack: TrackModel? = playbackUiState.currentQueueItem?.track
    val isCurrentTrackLiked by viewModel.isCurrentTrackLiked.collectAsState()

    var showQueueBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        snapshotFlow { playbackUiState.currentQueueItem?.track }
            .collect { track ->
                if (track == null) {
                    navController.popBackStack()
                }
            }
    }

    if (currentTrack == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                navController = navController,
                title = currentTrack.title
            )
        },
        bottomBar = {
            TrackDetailsPlayerControls(
                playbackUiState = playbackUiState,
                onTogglePlayPause = { scope.launch { viewModel.togglePlayback(currentTrack) } },
                onPreviousClick = viewModel::playPreviousTrack,
                onNextClick = viewModel::playNextTrack,
                onShuffleClick = viewModel::toggleShuffleMode,
                onRepeatModeClick = viewModel::toggleRepeatMode,
                onQueueClick = { showQueueBottomSheet = true },
                onLikeClick = { viewModel.toggleAddToLiked(currentTrack) },
                isTrackLiked = isCurrentTrackLiked,
                onAddToQueue = viewModel::addTrackToQueue,
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
    if (showQueueBottomSheet) {
        QueueBottomSheet(
            playbackUiState = playbackUiState,
            onClearQueueClicked = viewModel::clearPlaybackQueue,
            onDismissRequest = { showQueueBottomSheet = false },
        )
    }
}

@Composable
fun TrackDetailsContent(
    track: TrackModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dominantBackgroundColor by remember { mutableStateOf(Color.Black) }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(track.bigPictureUri)
            .size(Size.ORIGINAL)
            .allowHardware(false)
            .build()
    )

    LaunchedEffect(painter.state) {
        if (painter.state is AsyncImagePainter.State.Success) {
            val bitmap =
                (painter.state as AsyncImagePainter.State.Success).result.drawable.toBitmap()
            Palette.from(bitmap).generate { palette ->
                palette?.let {
                    // try to get dominant color else use vibrant or muted
                    dominantBackgroundColor = it.dominantSwatch?.rgb?.let { color -> Color(color) }
                        ?: it.vibrantSwatch?.rgb?.let { color -> Color(color) }
                                ?: it.mutedSwatch?.rgb?.let { color -> Color(color) }
                                ?: Color.Black
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dominantBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            dominantBackgroundColor.copy(alpha = 0f),
                            Color.Black.copy(alpha = 0.5f)
                        ),
                        startY = 0.5f
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Main Album Art
            Image(
                painter = painter,
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(300.dp)
                    .aspectRatio(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            // Track Title and Artist
            Row(verticalAlignment = Alignment.CenterVertically) {
                track.isExplicit?.let {
                    if (it) {
                        Icon(
                            imageVector = Icons.Filled.Explicit,
                            contentDescription = null
                        )
                    }
                }
                Text(
                    text = track.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            track.contributors.forEachIndexed { index, contributor ->
                Text(
                    text = contributor.name,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
    onShuffleClick: () -> Unit,
    onRepeatModeClick: () -> Unit,
    onQueueClick: () -> Unit,
    onLikeClick: () -> Unit,
    isTrackLiked: Boolean,
    onAddToQueue: (TrackModel) -> Unit,
    onSeek: (Long) -> Unit,
    currentPosition: Long,
    trackDuration: Long,
    modifier: Modifier = Modifier
) {
    val isPlaying = playbackUiState.isPlaying
    val track = playbackUiState.currentQueueItem

    val isShuffleOn = playbackUiState.isShuffleModeEnabled
    var currentRepeatMode = playbackUiState.repeatMode

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
        // Three icons at the top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // queue
            IconButton(onClick = onQueueClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_queue),
                    contentDescription = "Queue",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            // like
            IconButton(onClick = onLikeClick) {
                val heartIcon = if (isTrackLiked) {
                    R.drawable.ic_heart_filled
                } else {
                    R.drawable.ic_heart
                }
                Icon(
                    painter = painterResource(heartIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // plus
            PublicTrackDropDownMenu(
                trackModel = playbackUiState.currentQueueItem.track,
                onAddToQueue = onAddToQueue,
                onLiked = { onLikeClick() },
                customIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        // Seek Bar (Progress Bar)
        val playbackProgress =
            remember(playbackUiState.currentPositionMs, playbackUiState.trackDurationMs) {
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
            IconButton(onClick = onShuffleClick) {
                Icon(
                    painterResource(if (isShuffleOn) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle),
                    contentDescription = "Shuffle",
                    tint = if (isShuffleOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Previous
            IconButton(onClick = onPreviousClick) {
                Icon(
                    painterResource(R.drawable.ic_skip_previous),
                    contentDescription = "Previous",
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
            IconButton(onClick = onRepeatModeClick) {
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