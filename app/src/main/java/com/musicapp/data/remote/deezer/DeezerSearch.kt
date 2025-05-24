package com.musicapp.data.remote.deezer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeezerSearchResponse(
    val data: List<DeezerTrackSummary>,
    val total: Int,
    val next: String?
)

@Serializable
data class DeezerTrackSummary(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("explicit_lyrics")
    val explicitLyrics: Boolean,
    @SerialName("preview")
    val preview: String,
    @SerialName("artist")
    val artist: DeezerArtist
)
