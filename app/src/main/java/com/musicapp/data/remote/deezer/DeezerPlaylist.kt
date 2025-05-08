package com.musicapp.data.remote.deezer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeezerPlaylist(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("picture_medium")
    val mediumPicture: String
)

@Serializable
data class DeezerPlaylistDetailed(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("duration")
    val duration: Long,
    @SerialName("nb_tracks")
    val trackCount: Int,
    @SerialName("picture_medium")
    val pictureMedium: String,
    @SerialName("tracks")
    val tracks: DeezerTracksWrapper
)