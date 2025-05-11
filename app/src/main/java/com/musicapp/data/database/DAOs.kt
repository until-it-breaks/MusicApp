package com.musicapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistsDAO {
    @Query("SELECT * FROM playlist WHERE playlist.ownerId = :userId AND playlist.isLikedPlaylist = 1")
    fun getLikedPlaylist(userId: String): Flow<Playlist>

    @Query("SELECT * FROM playlist WHERE playlist.ownerId = :userId AND playlist.isHistoryPlaylist = 1")
    fun getHistoryPlaylist(userId: String): Flow<Playlist>

    @Query("SELECT * FROM playlist WHERE playlist.ownerId = :userId")
    fun getUserPlaylists(userId: String): Flow<List<Playlist>>

    @Upsert()
    suspend fun addPlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
}

@Dao
interface TracksDAO {
    @Query("SELECT * FROM track WHERE track.playlistId = :playlistId")
    fun getPlaylistTracks(playlistId: Long): Flow<List<Track>>

    @Upsert
    suspend fun addTrack(track: Track)

    @Delete
    suspend fun deleteTrack(track: Track)
}

@Dao
interface UsersDAO {
    @Query("SELECT * FROM user")
    fun getUsers(): Flow<List<User>>

    @Upsert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}