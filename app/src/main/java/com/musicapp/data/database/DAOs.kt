package com.musicapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface TracksDAO {
    @Upsert
    suspend fun upsertTrack(track: Track)

    @Delete
    suspend fun deleteTrack(track: Track)
}

@Dao
interface UsersDAO {
    @Query("SELECT * FROM user")
    suspend fun getUsers(): List<User>

    @Upsert
    suspend fun upsertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface PlaylistsDAO {
    @Query("SELECT * FROM playlist WHERE playlist.ownerId = :userId")
    suspend fun getUserPlaylists(userId: String): List<Playlist>

    @Query("SELECT * FROM playlist WHERE playlist.playlistId = :playlistId")
    suspend fun getPlaylistWithTracks(playlistId: String): PlaylistWithTracks

    @Upsert()
    suspend fun upsertPlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
}

@Dao
interface LikedTracksDAO {
    @Query("SELECT * FROM likedtracksplaylist WHERE likedtracksplaylist.ownerId = :userId")
    suspend fun getLikedTracksPlaylist(userId: String): LikedTracksPlaylist

    @Query("SELECT * FROM likedtracksplaylist WHERE likedtracksplaylist.ownerId = :userId")
    suspend fun getLikedTracksPlaylistWithTracks(userId: String): LikedTracksPlaylistWithTracks

    @Upsert
    suspend fun upsertLikedTracksPlaylist(likedTracksPlaylist: LikedTracksPlaylist)

    @Delete
    suspend fun deleteLikedTracksPlaylist(likedTracksPlaylist: LikedTracksPlaylist)
}

@Dao
interface TrackHistoryDAO {
    @Query("SELECT * FROM trackhistory WHERE trackhistory.ownerId = :userId")
    suspend fun getTrackHistory(userId: String): TrackHistory

    @Query("SELECT * FROM trackhistory WHERE trackhistory.ownerId = :userId")
    suspend fun getTrackHistoryWithTracks(userId: String): TrackHistoryWithTracks

    @Insert
    suspend fun upsertTrackHistory(trackHistory: TrackHistory)

    @Delete
    suspend fun deleteTrackHistory(trackHistory: TrackHistory)
}