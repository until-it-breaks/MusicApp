package com.musicapp.data.models

import com.musicapp.data.database.TrackHistory
import com.musicapp.data.repositories.TrackHistoryWithTracks
import com.musicapp.data.repositories.TrackHistoryWithTracksAndArtists

data class TrackHistoryModel(
    val ownerId: String,
    val lastEditTime: Long,
    val tracks: List<TrackModel> = emptyList()
)

fun TrackHistory.toModel(): TrackHistoryModel {
    return TrackHistoryModel(
        ownerId = ownerId,
        lastEditTime = lastEditTime
    )
}

fun TrackHistoryWithTracks.toModel(): TrackHistoryModel {
    return TrackHistoryModel(
        ownerId = playlist.ownerId,
        lastEditTime = playlist.lastEditTime,
        tracks = tracks.map { it.toModel() }
    )
}

fun TrackHistoryWithTracksAndArtists.toModel(): TrackHistoryModel {
    return TrackHistoryModel(
        ownerId = playlist.ownerId,
        lastEditTime = playlist.lastEditTime,
        tracks = tracks.map { it.toModel() }
    )
}