package com.musicapp.ui.models

import com.musicapp.data.database.Playlist
import com.musicapp.data.database.PlaylistWithTracks

data class UserPlaylistModel (
    val id: String,
    val ownerId: String,
    val name: String,
    val tracks: List<TrackModel> = emptyList(),
    val lastEditTime: Long
)

fun Playlist.toModel(): UserPlaylistModel {
    return UserPlaylistModel(
        id = playlistId,
        ownerId = ownerId,
        name = name,
        lastEditTime = lastEditTime
    )
}

fun PlaylistWithTracks.toModel(): UserPlaylistModel {
    return UserPlaylistModel(
        id = playlist.playlistId,
        ownerId = playlist.ownerId,
        name = playlist.name,
        tracks = tracks.map { it.toModel() },
        lastEditTime = playlist.lastEditTime
    )
}