package com.musicapp.data.repositories

import com.musicapp.data.database.Track
import com.musicapp.data.database.TrackHistory
import com.musicapp.data.database.TrackHistoryDAO
import com.musicapp.data.database.TrackHistoryTrackCrossRef
import com.musicapp.ui.models.TrackHistoryModel
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

data class TrackHistoryWithTracks(
    val playlist: TrackHistory,
    val tracks: List<Track>
)

data class TrackHistoryWithTracksAndArtists(
    val playlist: TrackHistory,
    val tracks: List<TrackWithArtists>
)

/**
 *  Repository for track history
 */
class TrackHistoryRepository(
    private val trackRepository: TracksRepository,
    private val trackHistoryDAO: TrackHistoryDAO
) {
    fun getTrackHistory(userId: String): Flow<TrackHistory> {
        return trackHistoryDAO.getTrackHistory(userId)
    }

    fun getTrackHistoryWithTracks(userId: String): Flow<TrackHistoryWithTracks> {
        val playlistFlow = trackHistoryDAO.getTrackHistory(userId)
        val tracksFlow = trackHistoryDAO.getTracksOfPlaylist(userId)

        return combine(playlistFlow, tracksFlow) { playlist, tracks ->
            TrackHistoryWithTracks(playlist, tracks)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistWithTracksAndArtists(userId: String): Flow<TrackHistoryWithTracksAndArtists> {
        val playlistFlow = trackHistoryDAO.getTrackHistory(userId)
        val tracksFlow = trackHistoryDAO.getTracksOfPlaylist(userId)

        return combine(playlistFlow, tracksFlow) { playlist, tracks ->
            playlist to tracks
        }.flatMapLatest { (playlist, tracks) ->
            val trackWithArtistFlows = tracks.map { track ->
                trackRepository.getTrackWithArtists(track.trackId)
            }

            if (trackWithArtistFlows.isEmpty()) {
                flowOf(TrackHistoryWithTracksAndArtists(playlist, emptyList()))
            } else {
                combine(trackWithArtistFlows) { trackWithArtistsArray ->
                    TrackHistoryWithTracksAndArtists(
                        playlist = playlist,
                        tracks = trackWithArtistsArray.toList()
                    )
                }
            }
        }
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