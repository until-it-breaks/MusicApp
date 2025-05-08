package com.musicapp.data.remote.deezer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class DeezerTracksWrapper(
    @SerialName("data")
    val data: List<DeezerTrack>
)