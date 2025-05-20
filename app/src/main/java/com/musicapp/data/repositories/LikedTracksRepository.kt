package com.musicapp.data.repositories

import com.musicapp.data.database.LikedTracksDAO
import com.musicapp.data.database.LikedTracksPlaylist
import com.musicapp.data.database.LikedTracksPlaylistTrackCrossRef
import com.musicapp.ui.models.LikedTracksPlaylistModel
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 *  Repository for liked tracks
 */
class LikedTracksRepository(
    private val likedTracksDAO: LikedTracksDAO,
    private val trackRepository: TracksRepository
) {
    fun getLikedTracksWithTracks(userId: String): Flow<LikedTracksPlaylistModel> {
        return likedTracksDAO.getLikedTracksPlaylistWithTracks(userId).map { it.toModel() }
    }

    suspend fun isTrackInLikedTracks(userId: String, track: TrackModel): Boolean {
        return likedTracksDAO.getTrackFromLikedTracksPlaylist(userId, track.id) != null
    }

    suspend fun upsertLikedTracksPlaylist(playlist: LikedTracksPlaylistModel) {
        val playlist = LikedTracksPlaylist(
            ownerId = playlist.ownerId,
            lastEditTime = System.currentTimeMillis()
        )
        likedTracksDAO.upsertLikedTracksPlaylist(playlist)
    }

    suspend fun addTrackToLikedTracksPlaylist(ownerId: String, track: TrackModel) {
        trackRepository.upsertTrack(track)
        likedTracksDAO.addTrackToLikedTracksPlaylist(
            LikedTracksPlaylistTrackCrossRef(
                ownerId,
                track.id
            )
        )
        likedTracksDAO.updateEditTime(ownerId)
    }

    suspend fun removeTrackFromLikedTracksPlaylist(ownerId: String, trackId: Long) {
        likedTracksDAO.deleteTrackFromLikedTracksPlaylist(
            LikedTracksPlaylistTrackCrossRef(
                ownerId,
                trackId
            )
        )
    }

    suspend fun clearLikedTracksPlaylist(userId: String) {
        likedTracksDAO.clearLikedTracksPlaylist(userId)
    }
}