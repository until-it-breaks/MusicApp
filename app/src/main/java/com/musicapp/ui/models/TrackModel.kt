package com.musicapp.ui.models

import android.net.Uri
import androidx.core.net.toUri
import com.musicapp.data.database.Track
import com.musicapp.data.remote.deezer.DeezerAlbumTrack
import com.musicapp.data.remote.deezer.DeezerTrackDetailed
import com.musicapp.data.remote.deezer.DeezerTrackSummary
import com.musicapp.data.repositories.TrackWithArtists

data class TrackModel(
    val id: Long,
    val title: String,
    val duration: Long? = null,
    val releaseDate: String? = null,
    val isExplicit: Boolean? = null,
    val smallPicture: Uri? = null,
    val mediumPicture: Uri? = null,
    val bigPicture: Uri? = null,
    val contributors: List<ArtistModel> = emptyList(),
    val previewUri: Uri? = null,
)

fun DeezerAlbumTrack.toModel(): TrackModel {
    return TrackModel(
        id = id,
        title = title,
        duration = duration,
        isExplicit = isExplicit,
        previewUri = preview.toUri()
    )
}

fun DeezerTrackDetailed.toModel(): TrackModel {
    return TrackModel(
        id = id,
        title = title,
        duration = duration,
        releaseDate = releaseDate,
        isExplicit = isExplicit,
        mediumPicture = album.mediumCover.toUri(),
        previewUri = preview.toUri(),
        contributors = contributors.map { it.toModel() }
    )
}

fun Track.toModel(): TrackModel {
    return TrackModel(
        id = trackId,
        title = title,
        duration = duration,
        releaseDate = releaseDate,
        isExplicit = isExplicit,
        smallPicture = smallPictureUri?.toUri(),
        mediumPicture = mediumPictureUri?.toUri(),
        bigPicture = bigPictureUri?.toUri(),
        contributors = emptyList(),
        previewUri = previewUri?.toUri()
    )
}

fun TrackWithArtists.toModel(): TrackModel {
    return TrackModel(
        id = track.trackId,
        title = track.title,
        duration = track.duration,
        releaseDate = track.releaseDate,
        isExplicit = track.isExplicit,
        smallPicture = track.smallPictureUri?.toUri(),
        mediumPicture = track.mediumPictureUri?.toUri(),
        bigPicture = track.bigPictureUri?.toUri(),
        contributors = artists.map { it.toModel() },
        previewUri = track.previewUri?.toUri()
    )
}

fun TrackModel.toDbEntity(): Track {
    return Track(
        trackId = id,
        title = title,
        duration = duration,
        releaseDate = releaseDate,
        isExplicit = isExplicit,
        previewUri = previewUri.toString(),
        storedPreviewUri = null, // TODO
        smallPictureUri = smallPicture.toString(),
        mediumPictureUri = mediumPicture.toString(),
        bigPictureUri = bigPicture.toString()
    )
}

fun DeezerTrackSummary.toModel(): TrackModel {
    return TrackModel(
        id = this.id,
        title = this.title,
        isExplicit = this.explicitLyrics,
        previewUri = this.preview.toUri(),
        contributors = listOf(this.artist.toModel())
    )
}