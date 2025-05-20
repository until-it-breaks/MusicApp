package com.musicapp.ui.models

import com.musicapp.data.database.TrackHistory
import com.musicapp.data.database.TrackHistoryWithTracks

data class TrackHistoryModel(
    val ownerId: String,
    val lastEditTime: Long,
    val tracks: List<TrackModel> = emptyList()
)

fun TrackHistory.toModel(): TrackHistoryModel {
    return TrackHistoryModel(
        ownerId = ownerId,
        lastEditTime = this@toModel.lastEditTime
    )
}

fun TrackHistoryWithTracks.toModel(): TrackHistoryModel {
    return TrackHistoryModel(
        ownerId = playlist.ownerId,
        lastEditTime = playlist.lastEditTime,
        tracks = tracks.map { it.toModel() }
    )
}