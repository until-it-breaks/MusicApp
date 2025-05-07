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
