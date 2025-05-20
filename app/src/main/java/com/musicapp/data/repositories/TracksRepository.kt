package com.musicapp.data.repositories

import com.musicapp.data.database.Track
import com.musicapp.data.database.TrackArtistCrossRef
import com.musicapp.data.database.TracksDAO
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toDbEntity

class TracksRepository(
    private val dao: TracksDAO,
) {
    fun getTrackById(trackId: Long) = dao.getTrackById(trackId)

    suspend fun getTrackArtists(trackId: Long) = dao.getTrackArtists(trackId)

    suspend fun upsertTrack(track: TrackModel) {
        dao.upsertTrack(track.toDbEntity())
        for (contributor in track.contributors) {
            dao.upsertArtist(contributor.toDbEntity())
            dao.addArtistToTrack(TrackArtistCrossRef(track.id, contributor.id))
        }
    }

    suspend fun deleteTrack(track: Track) = dao.deleteTrack(track)
}