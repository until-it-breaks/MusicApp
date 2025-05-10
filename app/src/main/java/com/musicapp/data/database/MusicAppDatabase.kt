package com.musicapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class, Track::class, Playlist::class], version = 2)
abstract class MusicAppDatabase: RoomDatabase() {
    abstract fun userDAO(): UsersDAO
    abstract fun playlistDAO(): PlaylistsDAO
    abstract fun trackDAO(): TracksDAO
}