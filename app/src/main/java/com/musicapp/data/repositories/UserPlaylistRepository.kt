package com.musicapp.data.repositories

import com.musicapp.data.database.Playlist
import com.musicapp.data.database.PlaylistTrackCrossRef
import com.musicapp.data.database.Track
import com.musicapp.data.database.UserPlaylistDAO
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.UserPlaylistModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

data class PlaylistWithTracks(
    val playlist: Playlist,
    val tracks: List<Track>
)

data class PlaylistWithTracksAndArtists(
    val playlist: Playlist,
    val tracks: List<TrackWithArtists>
)

/**
 *  Repository for normal playlists
 */
class UserPlaylistRepository(
    private val playlistDAO: UserPlaylistDAO,
    private val trackRepository: TracksRepository
) {
    fun getPlaylists(playlistId: String): Flow<List<Playlist>> {
        return playlistDAO.getPlaylists(playlistId)
    }

    fun getPlaylistWithTracksFlow(playlistId: String): Flow<PlaylistWithTracks> {
        val playlistFlow = playlistDAO.getPlaylist(playlistId)
        val tracksFlow = playlistDAO.getTracksOfPlaylist(playlistId)

        return combine(playlistFlow, tracksFlow) { playlist, tracks ->
            PlaylistWithTracks(playlist, tracks)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistWithTracksAndArtists(playlistId: String): Flow<PlaylistWithTracksAndArtists> {
        val playlistFlow = playlistDAO.getPlaylist(playlistId)
        val tracksFlow = playlistDAO.getTracksOfPlaylist(playlistId)

        return combine(playlistFlow, tracksFlow) { playlist, tracks ->
            playlist to tracks
        }.flatMapLatest { (playlist, tracks) ->
            val trackWithArtistFlows = tracks.map { track ->
                trackRepository.getTrackWithArtists(track.trackId)
            }

            if (trackWithArtistFlows.isEmpty()) {
                flowOf(PlaylistWithTracksAndArtists(playlist, emptyList()))
            } else {
                combine(trackWithArtistFlows) { trackWithArtistsArray ->
                    PlaylistWithTracksAndArtists(
                        playlist = playlist,
                        tracks = trackWithArtistsArray.toList()
                    )
                }
            }
        }
    }

    suspend fun isTrackInPlaylist(playlistId: String, track: TrackModel): Boolean {
        return playlistDAO.getTrackFromPlaylist(playlistId, track.id) != null
    }

    suspend fun upsertPlaylist(playlist: UserPlaylistModel) {
        val playlist = Playlist(
            playlistId = playlist.id,
            ownerId = playlist.ownerId,
            name = playlist.name,
            lastEditTime = System.currentTimeMillis()
        )
        playlistDAO.upsertPlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: String, track: TrackModel) {
        trackRepository.upsertTrack(track)
        playlistDAO.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, track.id))
        playlistDAO.updateEditTime(playlistId)
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: Long) {
        playlistDAO.deleteTrackFromPlaylist(PlaylistTrackCrossRef(playlistId, trackId))
    }

    suspend fun clearPlaylist(playlistId: String) {
        playlistDAO.clearPlaylist(playlistId)
    }

    suspend fun deletePlaylist(playlistId: String) {
        playlistDAO.deletePlaylist(playlistId)
    }
}