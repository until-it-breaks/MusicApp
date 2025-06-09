package com.musicapp.ui.models

import com.musicapp.data.database.LikedPlaylist
import com.musicapp.data.repositories.LikedPlaylistWithTracks
import com.musicapp.data.repositories.LikedPlaylistWithTracksAndArtists

data class LikedTracksPlaylistModel(
    val ownerId: String,
    val lastEditTime: Long,
    val tracks: List<TrackModel> = emptyList()
)

fun LikedPlaylist.toModel(): LikedTracksPlaylistModel {
    return LikedTracksPlaylistModel(
        ownerId = ownerId,
        lastEditTime = lastEditTime
    )
}

fun LikedPlaylistWithTracks.toModel(): LikedTracksPlaylistModel {
    return LikedTracksPlaylistModel(
        ownerId = playlist.ownerId,
        lastEditTime = playlist.lastEditTime,
        tracks = tracks.map { it.toModel() }
    )
}

fun LikedPlaylistWithTracksAndArtists.toModel(): LikedTracksPlaylistModel {
    return LikedTracksPlaylistModel(
        ownerId = playlist.ownerId,
        lastEditTime = playlist.lastEditTime,
        tracks = tracks.map { it.toModel() }
    )
}