package com.musicapp.data.remote.deezer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeezerCreator(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String
)
