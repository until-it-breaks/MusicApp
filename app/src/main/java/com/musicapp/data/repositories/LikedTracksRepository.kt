package com.musicapp.data.repositories

import androidx.room.withTransaction
import com.musicapp.data.database.LikedPlaylist
import com.musicapp.data.database.LikedPlaylistDAO
import com.musicapp.data.database.LikedPlaylistTrackCrossRef
import com.musicapp.data.database.MusicAppDatabase
import com.musicapp.data.database.Track
import com.musicapp.data.models.TrackModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

data class LikedPlaylistWithTracks(
    val playlist: LikedPlaylist,
    val tracks: List<Track>
)

data class LikedPlaylistWithTracksAndArtists(
    val playlist: LikedPlaylist,
    val tracks: List<TrackWithArtists>
)

/**
 *  Repository for liked tracks.
 */
class LikedTracksRepository(
    private val db: MusicAppDatabase,
    private val likedPlaylistDAO: LikedPlaylistDAO,
    private val trackRepository: TracksRepository
) {

    /**
     * Returns a flow of liked playlist with tracks.
     */
    fun getPlaylistWithTracks(playlistId: String): Flow<LikedPlaylistWithTracks> {
        val playlistFlow = likedPlaylistDAO.getLikedPlaylist(playlistId)
        val tracksFlow = likedPlaylistDAO.getTracksOfPlaylist(playlistId)

        return combine(playlistFlow, tracksFlow) { playlist, tracks ->
            LikedPlaylistWithTracks(playlist, tracks)
        }
    }

    /**
     * Returns a flow of liked playlist with tracks and contributors.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistWithTracksAndArtists(playlistId: String): Flow<LikedPlaylistWithTracksAndArtists> {
        val playlistFlow = likedPlaylistDAO.getLikedPlaylist(playlistId)
        val tracksFlow = likedPlaylistDAO.getTracksOfPlaylist(playlistId)

        return combine(playlistFlow, tracksFlow) { playlist, tracks ->
            playlist to tracks
        }.flatMapLatest { (playlist, tracks) ->
            val trackWithArtistFlows = tracks.map { track ->
                trackRepository.getTrackWithArtists(track.trackId)
            }

            if (trackWithArtistFlows.isEmpty()) {
                flowOf(LikedPlaylistWithTracksAndArtists(playlist, emptyList()))
            } else {
                combine(trackWithArtistFlows) { trackWithArtistsArray ->
                    LikedPlaylistWithTracksAndArtists(
                        playlist = playlist,
                        tracks = trackWithArtistsArray.toList()
                    )
                }
            }
        }
    }

    /**
     * Returns true if a given track is in the liked tracks of a given user, false otherwise.
     */
    suspend fun isTrackInLikedTracks(playlistId: String, track: TrackModel): Boolean {
        return likedPlaylistDAO.getTrackFromLikedTracks(playlistId, track.id) != null
    }

    /**
     * Adds a track to a given user's liked tracks.
     */
    suspend fun addTrackToLikedTracks(playlistId: String, track: TrackModel) {
        db.withTransaction {
            trackRepository.upsertTrack(track)
            likedPlaylistDAO.addTrackToLikedTracks(LikedPlaylistTrackCrossRef(playlistId, track.id, System.currentTimeMillis()))
            likedPlaylistDAO.updateEditTime(playlistId)
        }
    }

    /**
     * Removes a track from a given user's liked tracks.
     */
    suspend fun removeTrackFromLikedTracks(playlistId: String, trackId: Long) {
        db.withTransaction {
            likedPlaylistDAO.deleteTrackFromLikedTracks(playlistId, trackId)
            likedPlaylistDAO.updateEditTime(playlistId)
        }
    }

    /**
     * Clears the liked tracks of a given user.
     */
    suspend fun clearLikedTracks(playlistId: String) {
        db.withTransaction {
            likedPlaylistDAO.clearLikedTracks(playlistId)
            likedPlaylistDAO.updateEditTime(playlistId)
        }
    }
}