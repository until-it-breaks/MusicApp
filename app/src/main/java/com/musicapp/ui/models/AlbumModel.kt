package com.musicapp.ui.models

import android.net.Uri
import androidx.core.net.toUri
import com.musicapp.data.remote.deezer.DeezerAlbumDetailed
import com.musicapp.data.remote.deezer.DeezerChartAlbum
import com.musicapp.data.remote.deezer.DeezerTrackAlbum

data class AlbumModel(
    val id: Long,
    val title: String,
    val smallCoverUri: Uri = Uri.EMPTY,
    val mediumCoverUri: Uri = Uri.EMPTY,
    val bigCoverUri: Uri = Uri.EMPTY,
    val label: String? = null,
    val trackCount: Int? = null,
    val duration: Long? = null,
    val releaseDate: String? = null,
    val isExplicit: Boolean? = null,
    val contributors: List<ArtistModel> = emptyList(),
    val tracks: List<TrackModel> = emptyList()
)

fun DeezerChartAlbum.toModel(): AlbumModel {
    return AlbumModel(
        id = id,
        title = title,
        smallCoverUri = smallCover?.toUri() ?: Uri.EMPTY,
        mediumCoverUri = mediumCover?.toUri() ?: Uri.EMPTY,
        bigCoverUri = bigCover?.toUri() ?: Uri.EMPTY,
        isExplicit = isExplicit
    )
}

fun DeezerTrackAlbum.toModel(): AlbumModel {
    return AlbumModel(
        id = id,
        title = title,
        smallCoverUri = smallCover?.toUri() ?: Uri.EMPTY,
        mediumCoverUri = mediumCover?.toUri() ?: Uri.EMPTY,
        bigCoverUri = bigCover?.toUri() ?: Uri.EMPTY
    )
}

fun DeezerAlbumDetailed.toModel(): AlbumModel {
    return AlbumModel(
        id = id,
        title = title,
        smallCoverUri = smallCover?.toUri() ?: Uri.EMPTY,
        mediumCoverUri = mediumCover?.toUri() ?: Uri.EMPTY,
        bigCoverUri = bigCover?.toUri() ?: Uri.EMPTY,
        label = label,
        trackCount = trackCount,
        duration = duration,
        releaseDate = releaseDate,
        isExplicit = isExplicit,
        contributors = contributors.map { it.toModel() },
        tracks = tracks.data.map { it.toModel() }
    )
}