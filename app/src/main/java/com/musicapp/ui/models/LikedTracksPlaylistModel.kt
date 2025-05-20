package com.musicapp.ui.models

import com.musicapp.data.database.LikedTracksPlaylist
import com.musicapp.data.database.LikedTracksPlaylistWithTracks

data class LikedTracksPlaylistModel(
    val ownerId: String,
    val lastEditTime: Long,
    val tracks: List<TrackModel> = emptyList()
)

fun LikedTracksPlaylist.toModel(): LikedTracksPlaylistModel {
    return LikedTracksPlaylistModel(
        ownerId = ownerId,
        lastEditTime = lastEditTime
    )
}

fun LikedTracksPlaylistWithTracks.toModel(): LikedTracksPlaylistModel {
    return LikedTracksPlaylistModel(
        ownerId = playlist.ownerId,
        lastEditTime = playlist.lastEditTime,
        tracks = tracks.map { it.toModel() }
    )
}