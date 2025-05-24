package com.musicapp.data.repositories

import androidx.room.withTransaction
import com.musicapp.data.database.MusicAppDatabase
import com.musicapp.data.database.Track
import com.musicapp.data.database.TrackHistory
import com.musicapp.data.database.TrackHistoryDAO
import com.musicapp.data.database.TrackHistoryTrackCrossRef
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
 *  Repository for track history.
 */
class TrackHistoryRepository(
    private val db: MusicAppDatabase,
    private val trackRepository: TracksRepository,
    private val trackHistoryDAO: TrackHistoryDAO
) {

    /**
     * Returns a flow of track history with tracks.
     */
    fun getTrackHistoryWithTracks(playlistId: String): Flow<TrackHistoryWithTracks> {
        val playlistFlow = trackHistoryDAO.getTrackHistory(playlistId)
        val tracksFlow = trackHistoryDAO.getTracksOfPlaylist(playlistId)

        return combine(playlistFlow, tracksFlow) { playlist, tracks ->
            TrackHistoryWithTracks(playlist, tracks)
        }
    }

    /**
     * Returns a flow of track history with tracks and contributors.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTrackHistoryWithTracksAndArtists(playlistId: String): Flow<TrackHistoryWithTracksAndArtists> {
        val playlistFlow = trackHistoryDAO.getTrackHistory(playlistId)
        val tracksFlow = trackHistoryDAO.getTracksOfPlaylist(playlistId)

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

    /**
     * Adds a track to a given user's track history.
     */
    suspend fun addTrackToTrackHistory(playlistId: String, track: TrackModel) {
        db.withTransaction {
            trackRepository.upsertTrack(track)
            trackHistoryDAO.addTrackToTrackHistory(TrackHistoryTrackCrossRef(playlistId, track.id, System.currentTimeMillis()))
            trackHistoryDAO.updateEditTime(playlistId)
        }
    }

    /**
     * Removes a track from a given user's track history.
     */
    suspend fun removeTrackFromTrackHistory(playlistId: String, trackId: Long) {
        db.withTransaction {
            trackHistoryDAO.deleteTrackFromTrackHistory(playlistId, trackId)
            trackHistoryDAO.updateEditTime(playlistId)
        }
    }

    /**
     * Clears the track history of a given user.
     */
    suspend fun clearTrackHistory(playlistId: String) {
        db.withTransaction {
            trackHistoryDAO.clearTrackHistory(playlistId)
            trackHistoryDAO.updateEditTime(playlistId)
        }
    }
}