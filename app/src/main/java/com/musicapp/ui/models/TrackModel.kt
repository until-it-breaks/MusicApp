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
    val smallPictureUri: Uri = Uri.EMPTY,
    val mediumPictureUri: Uri = Uri.EMPTY,
    val bigPictureUri: Uri = Uri.EMPTY,
    val contributors: List<ArtistModel> = emptyList(),
    val previewUri: Uri = Uri.EMPTY,
)

fun DeezerAlbumTrack.toModel(): TrackModel {
    return TrackModel(
        id = id,
        title = title,
        duration = duration,
        isExplicit = isExplicit,
        previewUri = preview?.toUri() ?: Uri.EMPTY
    )
}

fun DeezerTrackDetailed.toModel(): TrackModel {
    return TrackModel(
        id = id,
        title = title,
        duration = duration,
        releaseDate = releaseDate,
        isExplicit = isExplicit,
        smallPictureUri = album.smallCover?.toUri() ?: Uri.EMPTY,
        mediumPictureUri = album.mediumCover?.toUri() ?: Uri.EMPTY,
        bigPictureUri = album.bigCover?.toUri() ?: Uri.EMPTY,
        previewUri = preview?.toUri() ?: Uri.EMPTY,
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
        smallPictureUri = smallPictureUri?.toUri() ?: Uri.EMPTY,
        mediumPictureUri = mediumPictureUri?.toUri() ?: Uri.EMPTY,
        bigPictureUri = bigPictureUri?.toUri() ?: Uri.EMPTY,
        contributors = emptyList(),
        previewUri = previewUri?.toUri() ?: Uri.EMPTY
    )
}

fun TrackWithArtists.toModel(): TrackModel {
    return TrackModel(
        id = track.trackId,
        title = track.title,
        duration = track.duration,
        releaseDate = track.releaseDate,
        isExplicit = track.isExplicit,
        smallPictureUri = track.smallPictureUri?.toUri() ?: Uri.EMPTY,
        mediumPictureUri = track.mediumPictureUri?.toUri() ?: Uri.EMPTY,
        bigPictureUri = track.bigPictureUri?.toUri() ?: Uri.EMPTY,
        contributors = artists.map { it.toModel() },
        previewUri = track.previewUri?.toUri() ?: Uri.EMPTY
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
        smallPictureUri = smallPictureUri.toString(),
        mediumPictureUri = mediumPictureUri.toString(),
        bigPictureUri = bigPictureUri.toString()
    )
}

fun DeezerTrackSummary.toModel(): TrackModel {
    return TrackModel(
        id = this.id,
        title = this.title,
        isExplicit = this.explicitLyrics,
        previewUri = this.preview.toUri(),
        smallPictureUri = this.album.smallCover?.toUri() ?: Uri.EMPTY,
        mediumPictureUri = this.album.mediumCover?.toUri() ?: Uri.EMPTY,
        bigPictureUri = this.album.bigCover?.toUri() ?: Uri.EMPTY,
        contributors = listOf(this.artist.toModel())
    )
}