package com.musicapp.data.models

import android.net.Uri
import androidx.core.net.toUri
import com.musicapp.data.remote.deezer.DeezerChartPlaylist
import com.musicapp.data.remote.deezer.DeezerPlaylistDetailed

data class PublicPlaylistModel(
    val id: Long,
    val title: String,
    val description: String? = null,
    val duration: Long? = null,
    val trackCount: Int? = null,
    val smallPictureUri: Uri = Uri.EMPTY,
    val mediumPictureUri: Uri = Uri.EMPTY,
    val bigPictureUri: Uri = Uri.EMPTY,
    val creator: CreatorModel? = null,
    val tracks: List<TrackModel> = emptyList()
)

fun DeezerChartPlaylist.toModel(): PublicPlaylistModel {
    return PublicPlaylistModel(
        id = id,
        title = title,
        smallPictureUri = smallPicture?.toUri() ?: Uri.EMPTY,
        mediumPictureUri = mediumPicture?.toUri() ?: Uri.EMPTY,
        bigPictureUri = bigPicture?.toUri() ?: Uri.EMPTY,
    )
}

fun DeezerPlaylistDetailed.toModel(): PublicPlaylistModel {
    return PublicPlaylistModel(
        id = id,
        title = title,
        description = description,
        duration = duration,
        trackCount = trackCount,
        smallPictureUri = smallPicture?.toUri() ?: Uri.EMPTY,
        mediumPictureUri = mediumPicture?.toUri() ?: Uri.EMPTY,
        bigPictureUri = bigPicture?.toUri() ?: Uri.EMPTY,
        creator = creator.toModel(),
        tracks = tracks.data.map { it.toModel() }
    )
}