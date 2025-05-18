package com.musicapp.ui.models

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
    val mediumPicture: Uri? = null,
    val bigPicture: Uri? = null,
    val creator: CreatorModel? = null,
    val tracks: List<TrackModel> = emptyList()
)

fun DeezerChartPlaylist.toModel(): PublicPlaylistModel {
    return PublicPlaylistModel(
        id = id,
        title = title,
        mediumPicture = this@toModel.mediumPicture.toUri()
    )
}

fun DeezerPlaylistDetailed.toModel(): PublicPlaylistModel {
    return PublicPlaylistModel(
        id = id,
        title = title,
        description = description,
        duration = duration,
        trackCount = trackCount,
        mediumPicture = mediumPicture.toUri(),
        bigPicture = bigPicture.toUri(),
        creator = creator.toModel(),
        tracks = tracks.data.map { it.toModel() }
    )
}
