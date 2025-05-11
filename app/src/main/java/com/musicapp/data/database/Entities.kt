package com.musicapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity
data class Track(
    @PrimaryKey
    val trackId: Long,
    @ColumnInfo
    val title: String,
    @ColumnInfo
    val duration: Long,
    @ColumnInfo
    val releaseDate: String,
    @ColumnInfo
    val explicitLyrics: Boolean
)

@Entity
data class User(
    @PrimaryKey
    val userId: String,
    @ColumnInfo
    val username: String,
    @ColumnInfo
    val email: String
)

@Entity()
data class Playlist(
    @PrimaryKey()
    val playlistId: String = UUID.randomUUID().toString(), // Should be better than a combined primary key, avoids offline to online synchronization issues across multiple devices.
    @ColumnInfo
    val ownerId: String,
    @ColumnInfo
    val name: String,
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
    @ColumnInfo
    val lastUpdateTime: String
)

@Entity(primaryKeys = ["ownerId", "trackId"])
data class LikedTracksTrackCrossRef(
    val ownerId: String,
    val trackId: Long
)

data class LikedTracksPlaylistWithTracks(
    @Embedded val playlist: LikedTracksPlaylist,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "trackId",
        associateBy = Junction(LikedTracksTrackCrossRef::class)
    )
    val tracks: List<Track>
)

@Entity
data class TrackHistory(
    @PrimaryKey
    val ownerId: String,
    @ColumnInfo
    val lastUpdateTime: String
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