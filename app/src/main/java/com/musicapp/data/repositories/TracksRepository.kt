package com.musicapp.data.repositories

import com.musicapp.data.database.Artist
import com.musicapp.data.database.Track
import com.musicapp.data.database.TrackArtistCrossRef
import com.musicapp.data.database.TrackDAO
import com.musicapp.ui.models.TrackModel
import com.musicapp.ui.models.toDbEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class TrackWithArtists(
    val track: Track,
    val artists: List<Artist>
)

class TracksRepository(
    private val dao: TrackDAO,
) {

    fun getTrackWithArtists(trackId: Long): Flow<TrackWithArtists> {
        val trackFlow = dao.getTrackFlow(trackId)
        val artistsFlow = dao.getTrackArtistsFlow(trackId)

        return combine(trackFlow, artistsFlow) { track, artists ->
            TrackWithArtists(track, artists)
        }
    }

    suspend fun upsertTrack(track: TrackModel) {
        dao.upsertTrack(track.toDbEntity())
        for (contributor in track.contributors) {
            dao.upsertArtist(contributor.toDbEntity())
            dao.addArtistToTrack(TrackArtistCrossRef(track.id, contributor.id))
        }
    }
}