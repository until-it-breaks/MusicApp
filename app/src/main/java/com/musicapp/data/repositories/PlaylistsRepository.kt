package com.musicapp.data.repositories

import android.content.ContentResolver
import com.musicapp.data.database.Playlist
import com.musicapp.data.database.PlaylistsDAO
import kotlinx.coroutines.flow.Flow

class PlaylistsRepository(
    private val dao: PlaylistsDAO,
    private val contentResolver: ContentResolver
) {
    fun getUserPlaylists(userId: String): Flow<List<Playlist>> = dao.getUserPlaylists(userId)

    suspend fun upsertPlaylist(playlist: Playlist) = dao.addPlaylist(playlist)

    suspend fun deleteTrack(playlist: Playlist) = dao.deletePlaylist(playlist)
}