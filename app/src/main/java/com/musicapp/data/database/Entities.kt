package com.musicapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Track

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

// Artist

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

// User

@Entity
data class User(
    @PrimaryKey
    val userId: String,
    val username: String,
    val email: String,
    val lastEditTime: Long
)

// Normal playlist

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
    val trackId: Long,
    val timeOfAddition: Long
)

// Liked Tracks

@Entity
data class LikedPlaylist(
    @PrimaryKey
    val ownerId: String,
    val lastEditTime: Long
)

@Entity(primaryKeys = ["ownerId", "trackId"])
data class LikedPlaylistTrackCrossRef(
    val ownerId: String,
    val trackId: Long,
    val timeOfAddition: Long
)

// Track history

@Entity
data class TrackHistory(
    @PrimaryKey
    val ownerId: String,
    val lastEditTime: Long
)

@Entity(primaryKeys = ["ownerId", "trackId"])
data class TrackHistoryTrackCrossRef(
    val ownerId: String,
    val trackId: Long,
    val timeOfAddition: Long
)