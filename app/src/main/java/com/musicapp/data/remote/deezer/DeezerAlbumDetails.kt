package com.musicapp.data.remote.deezer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeezerAlbumDetails(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("cover_medium")
    val coverMedium: String,
    @SerialName("label")
    val label: String,
    @SerialName("duration")
    val duration: Long,
    @SerialName("explicit_lyrics")
    val explicitLyrics: Boolean,
    @SerialName("tracks")
    val tracks: DeezerTracksDataWrapper
)

@Serializable
data class DeezerTracksDataWrapper(
    @SerialName("data")
    val data: List<DeezerTrack>
)

@Serializable
data class DeezerTrack(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("duration")
    val duration: Long,
    @SerialName("explicit_lyrics")
    val explicitLyrics: Boolean,
    @SerialName("preview")
    val preview: String
)