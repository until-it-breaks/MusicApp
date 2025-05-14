package com.musicapp.data.remote.deezer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeezerArtist(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("picture_small")
    val smallPicture: String,
    @SerialName("picture_medium")
    val mediumPicture: String,
    @SerialName("picture_big")
    val bigPicture: String
)
