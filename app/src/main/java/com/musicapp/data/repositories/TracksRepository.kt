package com.musicapp.data.repositories

import android.content.ContentResolver
import com.musicapp.data.database.Track
import com.musicapp.data.database.TracksDAO
import kotlinx.coroutines.flow.Flow

class TracksRepository(
    private val dao: TracksDAO,
    private val contentResolver: ContentResolver
) {
    fun getPlaylistTracks(playlistId: Long): Flow<List<Track>> = dao.getPlaylistTracks(playlistId)

    suspend fun upsertTrack(track: Track) = dao.addTrack(track)

    suspend fun deleteTrack(track: Track) = dao.deleteTrack(track)
}