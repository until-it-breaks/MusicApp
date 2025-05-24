package com.musicapp.ui.models

import androidx.core.net.toUri
import com.musicapp.data.database.Playlist
import com.musicapp.data.repositories.PlaylistWithTracks
import com.musicapp.data.repositories.PlaylistWithTracksAndArtists

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

fun PlaylistWithTracksAndArtists.toModel(): UserPlaylistModel {
    return UserPlaylistModel(
        id = playlist.playlistId,
        ownerId = playlist.ownerId,
        name = playlist.name,
        lastEditTime = playlist.lastEditTime,
        tracks = tracks.map {
            TrackModel(
                id = it.track.trackId,
                title = it.track.title,
                duration = it.track.duration,
                releaseDate = it.track.releaseDate,
                isExplicit = it.track.isExplicit,
                smallPicture = it.track.smallPictureUri?.toUri(),
                mediumPicture = it.track.mediumPictureUri?.toUri(),
                bigPicture = it.track.bigPictureUri?.toUri(),
                contributors = it.artists.map { it.toModel() },
                previewUri = it.track.previewUri?.toUri()
            )
        }
    )
}