package com.musicapp.ui.models

import android.net.Uri
import androidx.core.net.toUri
import com.musicapp.data.database.Track
import com.musicapp.data.remote.deezer.DeezerChartTrack
import com.musicapp.data.remote.deezer.DeezerTrackDetailed

data class TrackModel(
    val id: Long,
    val title: String,
    val duration: Long? = null,
    val releaseDate: String? = null,
    val isExplicit: Boolean? = null,
    val contributors: List<ArtistModel> = emptyList(),
    val album: AlbumModel? = null,
    val previewUri: Uri? = null,
)

fun DeezerChartTrack.toModel(): TrackModel {
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
        previewUri = preview.toUri(),
        contributors = contributors.map { it.toModel() },
        album = album.toModel()
    )
}

fun Track.toModel(): TrackModel {
    return TrackModel(
        id = trackId,
        title = title,
        duration = duration,
        releaseDate = releaseDate,
        isExplicit = isExplicit
    )
}