package com.musicapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Track(
    @PrimaryKey
    val id: Long,
    @ColumnInfo
    val title: String,
    @ColumnInfo
    val duration: Long,
    @ColumnInfo
    val releaseData: String,
    @ColumnInfo
    val explicitLyrics: Boolean,
    @ColumnInfo
    val playlistId: Long
)

@Entity
data class User(
    @PrimaryKey
    val uid: String,
    @ColumnInfo
    val name: String
)

@Entity
data class Playlist(
    @PrimaryKey
    val id: Long,
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val ownerId: String,
    @ColumnInfo
    val isLikedPlaylist: Boolean,
    @ColumnInfo
    val isHistoryPlaylist: Boolean
)