package com.musicapp.data.remote.deezer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeezerAlbumTrack(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("duration")
    val duration: Long,
    @SerialName("explicit_lyrics")
    val isExplicit: Boolean,
    @SerialName("preview")
    val preview: String?
)

@Serializable
data class DeezerTrackDetailed(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("duration")
    val duration: Long,
    @SerialName("release_date")
    val releaseDate: String,
    @SerialName("explicit_lyrics")
    val isExplicit: Boolean,
    @SerialName("preview")
    val preview: String?,
    @SerialName("contributors")
    val contributors: List<DeezerArtist>,
    @SerialName("album")
    val album: DeezerTrackAlbum
)
