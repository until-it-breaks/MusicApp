package com.musicapp.data.repositories

import com.musicapp.data.database.Playlist
import com.musicapp.data.database.PlaylistTrackCrossRef
import com.musicapp.data.database.UserPlaylistDAO
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.UserPlaylistModel
import com.musicapp.ui.models.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 *  Repository for normal playlists
 */
class UserPlaylistRepository(
    private val playlistDAO: UserPlaylistDAO,
    private val trackRepository: TracksRepository
) {
    fun getUserPlaylistsWithTracks(userId: String): Flow<List<UserPlaylistModel>> {
        return playlistDAO.getPlaylistsWithTracks(userId).map { it.map { it.toModel() } }
    }

    fun getUserPlaylistWithTracks(playlistId: String): Flow<UserPlaylistModel> {
        return playlistDAO.getPlaylistWithTracks(playlistId).map {
            it.toModel()
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