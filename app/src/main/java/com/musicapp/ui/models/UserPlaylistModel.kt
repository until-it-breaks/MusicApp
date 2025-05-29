package com.musicapp.ui.models

import android.net.Uri
import androidx.core.net.toUri
import com.musicapp.data.database.Playlist
import com.musicapp.data.repositories.PlaylistWithTracks
import com.musicapp.data.repositories.PlaylistWithTracksAndArtists

data class UserPlaylistModel (
    val id: String,
    val ownerId: String,
    val name: String,
    val tracks: List<TrackModel> = emptyList(),
    val playlistPictureUri: Uri = Uri.EMPTY,
    val lastEditTime: Long
)

fun Playlist.toModel(): UserPlaylistModel {
    return UserPlaylistModel(
        id = playlistId,
        ownerId = ownerId,
        name = name,
        playlistPictureUri = pictureUri?.toUri() ?: Uri.EMPTY,
        lastEditTime = lastEditTime
    )
}

fun PlaylistWithTracks.toModel(): UserPlaylistModel {
    return UserPlaylistModel(
        id = playlist.playlistId,
        ownerId = playlist.ownerId,
        name = playlist.name,
        tracks = tracks.map { it.toModel() },
        playlistPictureUri = playlist.pictureUri?.toUri() ?: Uri.EMPTY,
        lastEditTime = playlist.lastEditTime
    )
}

fun PlaylistWithTracksAndArtists.toModel(): UserPlaylistModel {
    return UserPlaylistModel(
        id = playlist.playlistId,
        ownerId = playlist.ownerId,
        name = playlist.name,
        playlistPictureUri = playlist.pictureUri?.toUri() ?: Uri.EMPTY,
        lastEditTime = playlist.lastEditTime,
        tracks = tracks.map { it.toModel() }
    )
}