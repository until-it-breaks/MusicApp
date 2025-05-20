package com.musicapp.data.repositories

import com.musicapp.data.database.TrackHistory
import com.musicapp.data.database.TrackHistoryDAO
import com.musicapp.data.database.TrackHistoryTrackCrossRef
import com.musicapp.ui.models.TrackHistoryModel
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 *  Repository for track history
 */
class TrackHistoryRepository(
    private val trackRepository: TracksRepository,
    private val trackHistoryDAO: TrackHistoryDAO
) {
    fun getTrackHistoryWithTracks(userId: String): Flow<TrackHistoryModel> {
        return trackHistoryDAO.getTrackHistoryWithTracks(userId).map { it.toModel() }
    }

    suspend fun upsertTrackHistory(trackHistory: TrackHistoryModel) {
        val trackHistory = TrackHistory(
            ownerId = trackHistory.ownerId,
            lastEditTime = System.currentTimeMillis()
        )
        trackHistoryDAO.upsertTrackHistory(trackHistory)
    }

    suspend fun addTrackToTrackHistory(ownerId: String, track: TrackModel) {
        trackRepository.upsertTrack(track)
        trackHistoryDAO.addTrackToTrackHistory(TrackHistoryTrackCrossRef(ownerId, track.id))
        trackHistoryDAO.updateEditTime(ownerId)
    }

    suspend fun removeTrackFromTrackHistory(ownerId: String, trackId: Long) {
        trackHistoryDAO.deleteTrackFromTrackHistory(TrackHistoryTrackCrossRef(ownerId, trackId))
    }

    suspend fun clearTrackHistory(userId: String) {
        trackHistoryDAO.clearTrackHistory(userId)
    }
}