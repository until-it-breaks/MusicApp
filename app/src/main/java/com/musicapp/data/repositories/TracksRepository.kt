package com.musicapp.data.repositories

import android.content.ContentResolver
import com.musicapp.data.database.Track
import com.musicapp.data.database.TracksDAO

class TracksRepository(
    private val dao: TracksDAO,
    private val contentResolver: ContentResolver
) {
    suspend fun upsertTrack(track: Track) = dao.upsertTrack(track)

    suspend fun deleteTrack(track: Track) = dao.deleteTrack(track)
}