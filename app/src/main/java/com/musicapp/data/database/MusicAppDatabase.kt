package com.musicapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Track::class,
        Artist::class,
        TrackArtistCrossRef::class,
        Playlist::class,
        PlaylistTrackCrossRef::class,
        LikedPlaylist::class,
        LikedPlaylistTrackCrossRef::class,
        TrackHistory::class,
        TrackHistoryTrackCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MusicAppDatabase: RoomDatabase() {
    abstract fun userDAO(): UserDAO
    abstract fun playlistDAO(): UserPlaylistDAO
    abstract fun trackDAO(): TrackDAO
    abstract fun likedPlaylistDAO(): LikedPlaylistDAO
    abstract fun trackHistoryDAO(): TrackHistoryDAO
}