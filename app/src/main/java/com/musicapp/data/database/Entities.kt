package com.musicapp.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class Track(
    @PrimaryKey
    val trackId: Long,
    val title: String,
    val duration: Long?,
    val releaseDate: String?,
    val isExplicit: Boolean?,
    val previewUri: String?,
    val storedPreviewUri: String?,
    val smallPictureUri: String?,
    val mediumPictureUri: String?,
    val bigPictureUri: String?
)

@Entity
data class Artist(
    @PrimaryKey
    val artistId: Long,
    val name: String,
    val smallPictureUri: String?,
    val mediumPictureUri: String?,
    val bigPictureUri: String?
)

@Entity(primaryKeys = ["trackId", "artistId"])
data class TrackArtistCrossRef(
    val trackId: Long,
    val artistId: Long
)

data class TrackWithArtists(
    @Embedded val track: Track,
    @Relation(
        parentColumn = "trackId",
        entityColumn = "artistId",
        associateBy = Junction(TrackArtistCrossRef::class)
    )
    val tracks: List<Artist>
)

@Entity
data class User(
    @PrimaryKey
    val userId: String,
    val username: String,
    val email: String,
    val lastEditTime: Long
)

@Entity
data class Playlist(
    @PrimaryKey()
    val playlistId: String,
    val ownerId: String,
    val name: String,
    val lastEditTime: Long
)

@Entity(primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: Long
)

data class PlaylistWithTracks(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "trackId",
        associateBy = Junction(PlaylistTrackCrossRef::class)
    )
    val tracks: List<Track>
)

@Entity
data class LikedTracksPlaylist(
    @PrimaryKey
    val ownerId: String,
    val lastEditTime: Long
)

@Entity(primaryKeys = ["ownerId", "trackId"])
data class LikedTracksPlaylistTrackCrossRef(
    val ownerId: String,
    val trackId: Long
)

data class LikedTracksPlaylistWithTracks(
    @Embedded val playlist: LikedTracksPlaylist,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "trackId",
        associateBy = Junction(LikedTracksPlaylistTrackCrossRef::class)
    )
    val tracks: List<Track>
)

@Entity
data class TrackHistory(
    @PrimaryKey
    val ownerId: String,
    val lastEditTime: Long
)

@Entity(primaryKeys = ["ownerId", "trackId"])
data class TrackHistoryTrackCrossRef(
    val ownerId: String,
    val trackId: Long
)

data class TrackHistoryWithTracks(
    @Embedded val playlist: TrackHistory,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "trackId",
        associateBy = Junction(TrackHistoryTrackCrossRef::class)
    )
    val tracks: List<Track>
)