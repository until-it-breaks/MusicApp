package com.musicapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDAO {
    @Query("SELECT * FROM track WHERE track.trackId = :trackId")
    fun getTrackFlow(trackId: Long): Flow<Track>

    @Query("""
    SELECT Artist.* FROM Artist, TrackArtistCrossRef WHERE Artist.artistId = TrackArtistCrossRef.artistId
    AND TrackArtistCrossRef.trackId = :trackId ORDER BY TrackArtistCrossRef.`order`
    """)
    fun getTrackArtistsFlow(trackId: Long): Flow<List<Artist>>

    @Upsert
    suspend fun upsertTrack(track: Track)

    @Upsert
    suspend fun upsertArtist(artist: Artist)

    @Upsert
    suspend fun addArtistToTrack(crossRef: TrackArtistCrossRef)

    @Query("DELETE FROM track WHERE trackId = :trackId")
    suspend fun deleteTrack(trackId: Long)
}

@Dao
interface UserDAO {
    @Query("SELECT * FROM user WHERE userId = :userId")
    fun getUser(userId: String): Flow<User>

    @Insert
    suspend fun insertUser(user: User)

    @Query("UPDATE user SET username = :username WHERE userId = :userId")
    suspend fun updateUsername(username: String, userId: String)

    @Query("UPDATE user SET profilePictureUri = :profilePictureUri WHERE userId = :userId")
    suspend fun updateProfilePicture(profilePictureUri: String, userId: String)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface UserPlaylistDAO {

    /**
     * Retrieves a playlist given an id.
     */
    @Query("SELECT * FROM playlist WHERE playlistId = :playlistId")
    fun getPlaylist(playlistId: String): Flow<Playlist?>

    /**
     * Retrieves the playlists of a given user
     */
    @Query("SELECT * FROM playlist WHERE ownerId = :ownerId")
    fun getPlaylists(ownerId: String): Flow<List<Playlist>>

    @Query("""
    SELECT Track.* FROM Track
    INNER JOIN PlaylistTrackCrossRef ON Track.trackId = PlaylistTrackCrossRef.trackId
    WHERE PlaylistTrackCrossRef.playlistId = :playlistId ORDER BY PlaylistTrackCrossRef.timeOfAddition
    """)
    fun getTracksOfPlaylist(playlistId: String): Flow<List<Track>>

    @Query("SELECT * FROM playlisttrackcrossref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun getTrackFromPlaylist(playlistId: String, trackId: Long): PlaylistTrackCrossRef?

    /**
     * Creates or updates a playlist (tracks not included).
     */
    @Insert()
    suspend fun insertPlaylist(playlist: Playlist)

    /**
     * Adds a track to a playlist
     */
    @Upsert
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    /**
     * Updates a playlist's name.
     */
    @Query("UPDATE playlist SET name = :name WHERE playlistId = :playlistId")
    suspend fun editName(playlistId: String, name: String)

    @Query("UPDATE playlist SET pictureUri = :pictureUri WHERE playlistId = :playlistId")
    suspend fun updatePlaylistPicture(playlistId: String, pictureUri: String)

    @Query("UPDATE playlist SET lastEditTime = :lastEditTime WHERE playlistId = :playlistId")
    suspend fun updateEditTime(playlistId: String, lastEditTime: Long = System.currentTimeMillis())

    /**
     * Deletes a single track from a playlist
     */
    @Query("DELETE FROM playlisttrackcrossref WHERE playlisttrackcrossref.playlistId = :playlistId AND playlisttrackcrossref.trackId = :trackId")
    suspend fun deleteTrackFromPlaylist(playlistId: String, trackId: Long)

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
interface LikedPlaylistDAO {

    /**
     * Retrieves a playlist given an id.
     */
    @Query("SELECT * FROM likedplaylist WHERE ownerId = :userId")
    fun getLikedPlaylist(userId: String): Flow<LikedPlaylist>

    @Query("SELECT * FROM likedplaylisttrackcrossref WHERE ownerId = :userId AND trackId = :trackId")
    suspend fun getTrackFromLikedTracks(userId: String, trackId: Long): LikedPlaylistTrackCrossRef?

    @Query("""
    SELECT Track.* FROM Track
    INNER JOIN LikedPlaylistTrackCrossRef ON Track.trackId = LikedPlaylistTrackCrossRef.trackId
    WHERE LikedPlaylistTrackCrossRef.ownerId = :userId ORDER BY LikedPlaylistTrackCrossRef.timeOfAddition
    """)
    fun getTracksOfPlaylist(userId: String): Flow<List<Track>>

    /**
     * Creates or updates a liked tracks playlist (tracks not included).
     */
    @Insert
    suspend fun insertLikedTracksPlaylist(likedTracksPlaylist: LikedPlaylist)

    /**
     * Adds a track to the user's liked tracks playlist.
     */
    @Upsert
    suspend fun addTrackToLikedTracks(crossRef: LikedPlaylistTrackCrossRef)

    @Query("UPDATE likedplaylist SET lastEditTime = :lastEditTime WHERE ownerId = :playlistId")
    suspend fun updateEditTime(playlistId: String, lastEditTime: Long = System.currentTimeMillis())

    /**
     * Deletes a single track from a user's liked tracks playlist
     */
    @Query("DELETE FROM likedplaylisttrackcrossref WHERE likedplaylisttrackcrossref.ownerId = :ownerId AND likedplaylisttrackcrossref.trackId = :trackId")
    suspend fun deleteTrackFromLikedTracks(ownerId: String, trackId: Long)

    /**
     * Deletes all tracks in a user's liked tracks playlist
     */
    @Query("DELETE FROM likedplaylisttrackcrossref WHERE ownerId = :ownerId")
    suspend fun clearLikedTracks(ownerId: String)
}

@Dao
interface TrackHistoryDAO {

    @Query("SELECT * FROM trackhistory WHERE ownerId = :userId")
    fun getTrackHistory(userId: String): Flow<TrackHistory>

    @Query("""
    SELECT Track.* FROM Track
    INNER JOIN TrackHistoryTrackCrossRef ON Track.trackId = TrackHistoryTrackCrossRef.trackId
    WHERE TrackHistoryTrackCrossRef.ownerId = :userId ORDER BY TrackHistoryTrackCrossRef.timeOfAddition
    """)
    fun getTracksOfPlaylist(userId: String): Flow<List<Track>>

    /**
     * Creates or updates a track history (tracks not included).
     */
    @Insert
    suspend fun insertTrackHistory(trackHistory: TrackHistory)

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
    @Query("DELETE FROM trackhistorytrackcrossref WHERE trackhistorytrackcrossref.ownerId = :ownerId AND trackhistorytrackcrossref.trackId = :trackId")
    suspend fun deleteTrackFromTrackHistory(ownerId: String, trackId: Long)

    /**
     * Deletes all tracks in a user's track history.
     */
    @Query("DELETE FROM trackhistorytrackcrossref WHERE ownerId = :ownerId")
    suspend fun clearTrackHistory(ownerId: String)
}