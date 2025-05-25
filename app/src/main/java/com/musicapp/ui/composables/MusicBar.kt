package com.musicapp.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicapp.playback.PlaybackUiState
import com.musicapp.ui.models.TrackModel

@Composable
fun BottomMusicBar(
    playbackState: PlaybackUiState,
    onTogglePlayback: (TrackModel) -> Unit,
    onAddToList: (TrackModel) -> Unit, // Action for the plus button
    onBarClick: () -> Unit, // open the track screen
    modifier: Modifier = Modifier
) {

    val isVisible = playbackState.currentTrack != null

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // Fixed height for the bar
                .background(MaterialTheme.colorScheme.surfaceVariant) // A subtle background color
                .clickable { onBarClick() } // Make the whole bar clickable
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Left: Song Title and Artist
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                playbackState.currentTrack?.let { track ->
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

            // Right: Plus and Play/Pause buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                playbackState.currentTrack?.let { track ->
                    IconButton(onClick = { onAddToList(track) }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add to playlist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    if (playbackState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { onTogglePlayback(track) }) {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(48.dp)) // Placeholder for balance, adjust as needed
        }
    }
}