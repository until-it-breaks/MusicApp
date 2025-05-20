package com.musicapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TracksDAO {
    @Query("SELECT * FROM track WHERE track.trackId = :trackId")
    fun getTrackById(trackId: Long): TrackWithArtists?

    @Upsert
    suspend fun upsertTrack(track: Track)

    @Query("SELECT a.* FROM Artist AS a, TrackArtistCrossRef AS t WHERE a.artistId = t.artistId AND t.trackId = :trackId")
    suspend fun getTrackArtists(trackId: Long): List<Artist>

    @Upsert
    suspend fun upsertArtist(artist: Artist)

    @Upsert
    suspend fun addArtistToTrack(crossRef: TrackArtistCrossRef)

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
interface UserPlaylistDAO {
    /**
     * Retrieves a playlist.
     */
    @Query("SELECT * FROM playlist WHERE ownerId = :ownerId")
    fun getPlaylistsWithTracks(ownerId: String): Flow<List<PlaylistWithTracks>>

    @Query("SELECT * FROM playlist WHERE playlistId = :playlistId")
    fun getPlaylistWithTracks(playlistId: String): Flow<PlaylistWithTracks>

    @Query("SELECT * FROM playlisttrackcrossref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun getTrackFromPlaylist(playlistId: String, trackId: Long): PlaylistTrackCrossRef?

    /**
     * Creates or updates a playlist (tracks not included).
     */
    @Upsert()
    suspend fun upsertPlaylist(playlist: Playlist)

    /**
     * Adds a track to a playlist
     */
    @Upsert
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Query("UPDATE playlist SET lastEditTime = :lastEditTime WHERE playlistId = :playlistId")
    suspend fun updateEditTime(playlistId: String, lastEditTime: Long = System.currentTimeMillis())

    /**
     * Deletes a single track from a playlist
     */
    @Delete
    suspend fun deleteTrackFromPlaylist(crossRef: PlaylistTrackCrossRef)

    /**
     * Deletes all tracks in a playlist
     */
    @Query("DELETE FROM playlisttrackcrossref WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: String)

    /**
     * Deletes a playlist
     */
    @Query("DELETE FROM playlist WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: String)
}

@Dao
interface LikedTracksDAO {
    /**
     * Retrieves the liked tracks playlist of a given user.
     */
    @Query("SELECT * FROM likedtracksplaylist WHERE ownerId = :userId")
    fun getLikedTracksPlaylistWithTracks(userId: String): Flow<LikedTracksPlaylistWithTracks>

    @Query("SELECT * FROM likedtracksplaylisttrackcrossref WHERE ownerId = :userId AND trackId = :trackId")
    suspend fun getTrackFromLikedTracksPlaylist(userId: String, trackId: Long): LikedTracksPlaylistTrackCrossRef?

    /**
     * Creates or updates a liked tracks playlist (tracks not included).
     */
    @Upsert
    suspend fun upsertLikedTracksPlaylist(likedTracksPlaylist: LikedTracksPlaylist)

    /**
     * Adds a track to the user's liked tracks playlist.
     */
    @Upsert
    suspend fun addTrackToLikedTracksPlaylist(crossRef: LikedTracksPlaylistTrackCrossRef)

    @Query("UPDATE likedtracksplaylist SET lastEditTime = :lastEditTime WHERE ownerId = :playlistId")
    suspend fun updateEditTime(playlistId: String, lastEditTime: Long = System.currentTimeMillis())

    /**
     * Deletes a single track from a user's liked tracks playlist
     */
    @Delete
    suspend fun deleteTrackFromLikedTracksPlaylist(crossRef: LikedTracksPlaylistTrackCrossRef)

    /**
     * Deletes all tracks in a user's liked tracks playlist
     */
    @Query("DELETE FROM likedtracksplaylisttrackcrossref WHERE ownerId = :ownerId")
    suspend fun clearLikedTracksPlaylist(ownerId: String)
}

@Dao
interface TrackHistoryDAO {
    /**
     * Retrieves the track history of a given user.
     */
    @Query("SELECT * FROM trackhistory WHERE ownerId = :userId")
    fun getTrackHistoryWithTracks(userId: String): Flow<TrackHistoryWithTracks>

    /**
     * Creates or updates a track history (tracks not included).
     */
    @Upsert
    suspend fun upsertTrackHistory(trackHistory: TrackHistory)

    /**
     * Adds a track to the user's track history.
     */
    @Upsert
    suspend fun addTrackToTrackHistory(crossRef: TrackHistoryTrackCrossRef)

    @Query("UPDATE trackhistory SET lastEditTime = :lastEditTime WHERE ownerId = :playlistId")
    suspend fun updateEditTime(playlistId: String, lastEditTime: Long = System.currentTimeMillis())

    /**
     * Deletes a single track from a user's track history.
     */
    @Delete
    suspend fun deleteTrackFromTrackHistory(crossRef: TrackHistoryTrackCrossRef)

    /**
     * Deletes all tracks in a user's track history.
     */
    @Query("DELETE FROM trackhistorytrackcrossref WHERE ownerId = :ownerId")
    suspend fun clearTrackHistory(ownerId: String)
}