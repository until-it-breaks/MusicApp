package com.musicapp.data.repositories

import com.musicapp.data.database.LikedPlaylistDAO
import com.musicapp.data.database.LikedPlaylist
import com.musicapp.data.database.LikedPlaylistTrackCrossRef
import com.musicapp.data.database.MusicAppDatabase
import com.musicapp.data.database.Track
import com.musicapp.ui.models.LikedTracksPlaylistModel
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class LikedPlaylistWithTracks(
    val playlist: LikedPlaylist,
    val tracks: List<Track>
)

data class LikedPlaylistWithTracksAndArtists(
    val playlist: LikedPlaylist,
    val tracks: List<TrackWithArtists>
)

/**
 *  Repository for liked tracks
 */
class LikedTracksRepository(
    private val db: MusicAppDatabase,
    private val likedPlaylistDAO: LikedPlaylistDAO,
    private val trackRepository: TracksRepository
) {

    fun getLikedTracksPlaylist(userId: String): Flow<LikedPlaylist> {
        return likedPlaylistDAO.getLikedPlaylist(userId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLikedPlaylistWithTracksFlow(userId: String): Flow<LikedPlaylistWithTracks> {
        val playlistFlow = likedPlaylistDAO.getLikedPlaylist(userId)
        return playlistFlow.flatMapLatest { playlist ->
            likedPlaylistDAO.getTracksOfPlaylist(userId).map { tracks ->
                LikedPlaylistWithTracks(playlist, tracks)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistWithTracksAndArtists(userId: String): Flow<LikedPlaylistWithTracksAndArtists> {
        val playlistFlow = likedPlaylistDAO.getLikedPlaylist(userId)
        val tracksFlow = likedPlaylistDAO.getTracksOfPlaylist(userId)

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

    suspend fun isTrackInLikedTracks(userId: String, track: TrackModel): Boolean {
        return likedPlaylistDAO.getTrackFromLikedTracksPlaylist(userId, track.id) != null
    }

    suspend fun upsertLikedTracksPlaylist(playlist: LikedTracksPlaylistModel) {
        val playlist = LikedPlaylist(
            ownerId = playlist.ownerId,
            lastEditTime = System.currentTimeMillis()
        )
        likedPlaylistDAO.upsertLikedTracksPlaylist(playlist)
    }

    suspend fun addTrackToLikedTracksPlaylist(userId: String, track: TrackModel) {
        trackRepository.upsertTrack(track)
        likedPlaylistDAO.addTrackToLikedTracksPlaylist(LikedPlaylistTrackCrossRef(userId, track.id, System.currentTimeMillis()))
        likedPlaylistDAO.updateEditTime(userId)
    }

    suspend fun removeTrackFromLikedTracksPlaylist(userId: String, trackId: Long) {
        likedPlaylistDAO.deleteTrackFromLikedTracksPlaylist(userId, trackId)
        likedPlaylistDAO.updateEditTime(userId)
    }

    suspend fun clearLikedTracksPlaylist(userId: String) {
        likedPlaylistDAO.clearLikedTracksPlaylist(userId)
        likedPlaylistDAO.updateEditTime(userId)
    }
}