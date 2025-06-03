package com.musicapp.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicapp.R
import com.musicapp.playback.PlaybackUiState
import com.musicapp.ui.models.TrackModel

@Composable
fun MusicBar(
    playbackState: PlaybackUiState,
    onTogglePlayback: (TrackModel) -> Unit,
    onStopClick: () -> Unit,
    onAddToList: (TrackModel) -> Unit,
    onQueueClick: () -> Unit,
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val isVisible = playbackState.currentTrack != null
    val track = playbackState.currentTrack

    if (isVisible) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onBarClick() }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            
            LoadableImage(track.mediumPictureUri, "Track picture", modifier = Modifier.size(48.dp), cornerRadius = 4.dp)

            // Left: Song Title and Artist
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                playbackState.currentTrack.let { track ->
                    val artists = track.contributors.joinToString(separator = ",") { it.name }
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = artists,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Right
            Row(verticalAlignment = Alignment.CenterVertically) {
                playbackState.currentTrack.let { track ->
                    // plus
                    IconButton(onClick = { onAddToList(track) }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add to playlist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // play/pause
                    if (playbackState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { onTogglePlayback(track) }) {
                            Icon(
                                painter = painterResource(if (playbackState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onStopClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_stop),
                            contentDescription = "Queue",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // queue
                    IconButton(onClick = onQueueClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_queue),
                            contentDescription = "Queue",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

        }
    }
}