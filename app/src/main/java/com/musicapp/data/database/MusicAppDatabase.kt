package com.musicapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities =
    [
        User::class,
        Track::class,
        Playlist::class,
        PlaylistTrackCrossRef::class,
        LikedTracksPlaylist::class,
        LikedTracksTrackCrossRef::class,
        TrackHistory::class,
        TrackHistoryTrackCrossRef::class
    ],
    version = 2)
abstract class MusicAppDatabase: RoomDatabase() {
    abstract fun userDAO(): UsersDAO
    abstract fun playlistDAO(): PlaylistsDAO
    abstract fun trackDAO(): TracksDAO
    abstract fun likedTracksDAO(): LikedTracksDAO
    abstract fun trackHistoryDAO(): TrackHistoryDAO
}