package com.musicapp.ui.models

import com.musicapp.data.database.TrackHistory
import com.musicapp.data.database.TrackHistoryWithTracks

data class TrackHistoryModel(
    val ownerId: String,
    val lastUpdateTime: String,
    val tracks: List<TrackModel> = emptyList()
)

fun TrackHistory.toModel(): TrackHistoryModel {
    return TrackHistoryModel(
        ownerId = ownerId,
        lastUpdateTime = lastUpdateTime
    )
}

fun TrackHistoryWithTracks.toModel(): TrackHistoryModel {
    return TrackHistoryModel(
        ownerId = playlist.ownerId,
        lastUpdateTime = playlist.lastUpdateTime,
        tracks = tracks.map { it.toModel() }
    )
}