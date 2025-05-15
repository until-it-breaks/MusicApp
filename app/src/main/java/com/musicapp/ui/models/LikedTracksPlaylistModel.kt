package com.musicapp.ui.models

import com.musicapp.data.database.LikedTracksPlaylist
import com.musicapp.data.database.LikedTracksPlaylistWithTracks

data class LikedTracksPlaylistModel(
    val ownerId: String,
    val lastUpdateTime: String,
    val tracks: List<TrackModel> = emptyList()
)

fun LikedTracksPlaylist.toModel(): LikedTracksPlaylistModel {
    return LikedTracksPlaylistModel(
        ownerId = ownerId,
        lastUpdateTime = lastUpdateTime
    )
}

fun LikedTracksPlaylistWithTracks.toModel(): LikedTracksPlaylistModel {
    return LikedTracksPlaylistModel(
        ownerId = playlist.ownerId,
        lastUpdateTime = playlist.lastUpdateTime,
        tracks = tracks.map { it.toModel() }
    )
}