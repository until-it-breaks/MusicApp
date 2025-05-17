package com.musicapp.data.repositories

import com.musicapp.data.database.LikedTracksDAO
import com.musicapp.data.database.LikedTracksPlaylist
import com.musicapp.data.database.LikedTracksPlaylistTrackCrossRef
import com.musicapp.data.database.Playlist
import com.musicapp.data.database.PlaylistTrackCrossRef
import com.musicapp.data.database.UserPlaylistDAO
import com.musicapp.data.database.TrackHistory
import com.musicapp.data.database.TrackHistoryDAO
import com.musicapp.data.database.TrackHistoryTrackCrossRef
import com.musicapp.ui.models.LikedTracksPlaylistModel
import com.musicapp.ui.models.TrackHistoryModel
import com.musicapp.ui.models.UserPlaylistModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistsRepository(
    private val playlistDAO: UserPlaylistDAO,
    private val likedTracksDAO: LikedTracksDAO,
    private val trackHistoryDAO: TrackHistoryDAO
) {
    // Normal playlists

    fun getUserPlaylistsWithTracks(userId: String): Flow<List<UserPlaylistModel>> {
        return playlistDAO.getPlaylistWithTracks(userId).map { it.map { it.toModel() } }
    }

    suspend fun upsertPlaylist(playlist: UserPlaylistModel) {
        val playlist = Playlist(
            playlistId = playlist.id,
            ownerId = playlist.ownerId,
            name = playlist.name
        )
        playlistDAO.upsertPlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: Long) {
        val crossRef = PlaylistTrackCrossRef(playlistId, trackId)
        playlistDAO.addTrackToPlaylist(crossRef)
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: Long) {
        val crossRef = PlaylistTrackCrossRef(playlistId, trackId)
        playlistDAO.deleteTrackFromPlaylist(crossRef)
    }

    suspend fun clearPlaylist(playlistId: String) {
        playlistDAO.clearPlaylist(playlistId)
    }

    suspend fun deletePlaylist(playlistId: String) {
        playlistDAO.deletePlaylist(playlistId)
    }

    /**
     * Liked Tracks Playlist
     */

    fun getLikedTracksWithTracks(userId: String): Flow<LikedTracksPlaylistModel> {
        return likedTracksDAO.getLikedTracksPlaylistWithTracks(userId).map { it.toModel() }
    }

    suspend fun upsertLikedTracksPlaylist(playlist: LikedTracksPlaylistModel) {
        val playlist = LikedTracksPlaylist(
            ownerId = playlist.ownerId,
            lastUpdateTime = "2025-01-01" // TODO change to a better date
        )
        likedTracksDAO.upsertLikedTracksPlaylist(playlist)
    }

    suspend fun addTrackToLikedTracksPlaylist(ownerId: String, trackId: Long) {
        val crossRef = LikedTracksPlaylistTrackCrossRef(ownerId, trackId)
        likedTracksDAO.addTrackToLikedTracksPlaylist(crossRef)
    }

    suspend fun removeTrackFromLikedTracksPlaylist(ownerId: String, trackId: Long) {
        val crossRef = LikedTracksPlaylistTrackCrossRef(ownerId, trackId)
        likedTracksDAO.deleteTrackFromLikedTracksPlaylist(crossRef)
    }

    suspend fun clearLikedTracksPlaylist(userId: String) {
        likedTracksDAO.clearLikedTracksPlaylist(userId)
    }

    /**
     * Track History
     */

    fun getTrackHistoryWithTracks(userId: String): Flow<TrackHistoryModel> {
        return trackHistoryDAO.getTrackHistoryWithTracks(userId).map { it.toModel() }
    }

    suspend fun upsertTrackHistory(trackHistory: TrackHistoryModel) {
        val trackHistory = TrackHistory(
            ownerId = trackHistory.ownerId,
            lastUpdateTime = "2025-01-01" // TODO change to a better date
        )
        trackHistoryDAO.upsertTrackHistory(trackHistory)
    }

    suspend fun addTrackToTrackHistory(ownerId: String, trackId: Long) {
        val crossRef = TrackHistoryTrackCrossRef(ownerId, trackId)
        trackHistoryDAO.addTrackToTrackHistory(crossRef)
    }

    suspend fun removeTrackFromTrackHistory(ownerId: String, trackId: Long) {
        val crossRef = TrackHistoryTrackCrossRef(ownerId, trackId)
        trackHistoryDAO.deleteTrackFromTrackHistory(crossRef)
    }

    suspend fun clearTrackHistory(userId: String) {
        trackHistoryDAO.clearTrackHistory(userId)
    }
}