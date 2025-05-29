package com.musicapp.ui.composables

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicapp.ui.models.TrackModel

enum class PlaylistType {
    DEFAULT,
    LIKED,
    HISTORY
}

/**
 * Simple card for playlist/album home representation
 */
@Composable
fun PlayListCard(modifier: Modifier = Modifier, title: String, imageUri: Uri? = null, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoadableImage(
                imageUri = imageUri,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

/**
 * Card for a given user's personal playlist (liked, history or generic playlist)
 */
@Composable
fun UserPlaylistCard(
    title: String,
    modifier: Modifier = Modifier,
    imageUri: Uri? = null,
    playlistType: PlaylistType = PlaylistType.DEFAULT,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            when(playlistType) {
                PlaylistType.DEFAULT -> LoadableImage(
                    imageUri = imageUri,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
                PlaylistType.LIKED -> Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
                PlaylistType.HISTORY -> Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = "Forward",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/**
 * Simple card for artist home representation
 */
@Composable
fun ArtistCard(modifier: Modifier = Modifier, title: String, imageUrl: Uri? = null, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.width(96.dp)
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LoadableImage(
                imageUri = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TrackCard(
    track: TrackModel,
    showPicture: Boolean = false,
    onTrackClick: (TrackModel) -> Unit,
    onArtistClick: (Long) -> Unit,
    extraMenu: @Composable () -> Unit
) {
    Card(
        onClick = { onTrackClick(track) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            if (showPicture) {
                LoadableImage(track.mediumPictureUri, "Track picture", modifier = Modifier.size(48.dp), cornerRadius = 4.dp)
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    track.isExplicit?.let {
                        if (it) {
                            Icon(
                                imageVector = Icons.Filled.Explicit,
                                contentDescription = null
                            )
                        }
                    }

                    track.contributors.forEachIndexed { index, contributor ->
                        Text(
                            text = contributor.name,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.Underline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable(onClick = { onArtistClick(contributor.id) })
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
            extraMenu()
        }
    }
}

@Composable
fun PlaylistCardToAddTo(
    title: String,
    imageUri: Uri = Uri.EMPTY,
    playlistType: PlaylistType = PlaylistType.DEFAULT,
    onClick: () -> Unit,
    enabled: Boolean,
    showCheck: Boolean
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (playlistType == PlaylistType.LIKED) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
            } else {
                LoadableImage(
                    imageUri = imageUri,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (showCheck || !enabled) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Forward",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun TrackCardToAdd(
    track: TrackModel
) {
    Card {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            LoadableImage(track.mediumPictureUri, "Track picture", modifier = Modifier.size(48.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}