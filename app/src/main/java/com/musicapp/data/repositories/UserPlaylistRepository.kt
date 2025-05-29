package com.musicapp.data.repositories

import android.content.Context
import androidx.core.net.toUri
import androidx.room.withTransaction
import com.musicapp.data.database.MusicAppDatabase
import com.musicapp.data.database.Playlist
import com.musicapp.data.database.PlaylistTrackCrossRef
import com.musicapp.data.database.Track
import com.musicapp.data.database.UserPlaylistDAO
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.UserPlaylistModel
import com.musicapp.util.combineBitmapsFromUris
import com.musicapp.util.deleteImageFromInternalStorage
import com.musicapp.util.saveBitmapToInternalStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

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
    private val db: MusicAppDatabase,
    private val playlistDAO: UserPlaylistDAO,
    private val trackRepository: TracksRepository,
    private val context: Context
) {
    fun getPlaylists(userId: String): Flow<List<Playlist>> {
        return playlistDAO.getPlaylists(userId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistsWithTracksFlow(userId: String): Flow<List<PlaylistWithTracks>> {
        return playlistDAO.getPlaylists(userId)
            .flatMapLatest { playlists ->
                if (playlists.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows: List<Flow<PlaylistWithTracks>> = playlists.map { playlist ->
                        playlistDAO.getTracksOfPlaylist(playlist.playlistId)
                            .map { tracks -> PlaylistWithTracks(playlist, tracks) }
                    }
                    combine(flows) { it.toList() }
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistWithTracksAndArtists(playlistId: String): Flow<PlaylistWithTracksAndArtists?> {
        val playlistFlow = playlistDAO.getPlaylist(playlistId)
        val tracksFlow = playlistDAO.getTracksOfPlaylist(playlistId)

        return combine(playlistFlow, tracksFlow) { playlist, tracks ->
            playlist to tracks
        }.flatMapLatest { (playlist, tracks) ->
            if (playlist == null) {
                flowOf(null)
            } else {
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
    }

    suspend fun editPlaylistName(playlistId: String, name: String) {
        db.withTransaction {
            playlistDAO.editName(playlistId, name)
            playlistDAO.updateEditTime(playlistId)
        }
    }

    suspend fun isTrackInPlaylist(playlistId: String, track: TrackModel): Boolean {
        return playlistDAO.getTrackFromPlaylist(playlistId, track.id) != null
    }

    suspend fun insertPlaylist(playlist: UserPlaylistModel) {
        val playlist = Playlist(
            playlistId = playlist.id,
            ownerId = playlist.ownerId,
            name = playlist.name,
            pictureUri = null,
            lastEditTime = System.currentTimeMillis()
        )
        playlistDAO.insertPlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: String, track: TrackModel) {
        db.withTransaction {
            trackRepository.upsertTrack(track)
            playlistDAO.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, track.id, System.currentTimeMillis()))
            updatePlaylistPicture(playlistId)
            playlistDAO.updateEditTime(playlistId)
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: Long) {
        playlistDAO.deleteTrackFromPlaylist(playlistId, trackId)
        updatePlaylistPicture(playlistId)
        playlistDAO.updateEditTime(playlistId)
    }

    suspend fun clearPlaylist(playlistId: String) {
        db.withTransaction {
            playlistDAO.clearPlaylist(playlistId)
            updatePlaylistPicture(playlistId)
            playlistDAO.updateEditTime(playlistId)
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        playlistDAO.deletePlaylist(playlistId)
    }

    private suspend fun updatePlaylistPicture(playlistId: String) {
        val tracks = playlistDAO.getTracksOfPlaylist(playlistId).first()
        val uris = tracks.take(4).mapNotNull { it.mediumPictureUri?.toUri() }
        val combined = combineBitmapsFromUris(context, uris)
        val playlist = playlistDAO.getPlaylist(playlistId).first()
        val oldUri = playlist?.pictureUri
        if (combined != null) {
            oldUri?.let { deleteImageFromInternalStorage(context, it) }
            val uri = saveBitmapToInternalStorage(context, combined, playlistId)
            playlistDAO.updatePlaylistPicture(playlistId, uri.toString())
        }
    }
}