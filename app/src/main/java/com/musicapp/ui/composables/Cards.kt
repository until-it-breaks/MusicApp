package com.musicapp.ui.composables

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.musicapp.R
import com.musicapp.data.remote.deezer.DeezerTrackDetailed

@Composable
fun PlayListCard(modifier: Modifier = Modifier, title: String, imageUri: Uri? = null, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoadableImage(
                imageUri = imageUri,
                contentDescription = stringResource(R.string.playlist_picture_description),
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun ArtistCard(modifier: Modifier = Modifier, title: String, imageUrl: String? = null, onClick: () -> Unit) {
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
                imageUri = imageUrl?.toUri(),
                contentDescription = stringResource(R.string.artist_picture_description),
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TrackCard(
    track: DeezerTrackDetailed,
    showPicture: Boolean = false,
    onTrackClick: (DeezerTrackDetailed) -> Unit,
    onArtistClick: (Long) -> Unit,
    onAddToLiked: (DeezerTrackDetailed) -> Unit
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
                LoadableImage(track.album.mediumCover.toUri(), "Track picture", modifier = Modifier.size(48.dp))
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (track.explicitLyrics) {
                        Icon(
                            imageVector = Icons.Filled.Explicit,
                            contentDescription = stringResource(R.string.explicit_description)
                        )
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
            TrackDropdownMenu(track, onAddToLiked = onAddToLiked)
        }
    }
}