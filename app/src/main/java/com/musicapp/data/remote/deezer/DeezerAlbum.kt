package com.musicapp.data.remote.deezer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeezerAlbum(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("cover_medium")
    val mediumCover: String,
    @SerialName("explicit_lyrics")
    val explicit: Boolean
)