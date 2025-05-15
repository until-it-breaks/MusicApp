package com.musicapp.data.remote.deezer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeezerChartAlbum(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("cover_medium")
    val mediumCover: String,
    @SerialName("explicit_lyrics")
    val isExplicit: Boolean
)

@Serializable
data class DeezerTrackAlbum(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("cover_medium")
    val mediumCover: String
)

@Serializable
data class DeezerAlbumDetailed(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("cover_medium")
    val mediumCover: String,
    @SerialName("cover_big")
    val bigCover: String,
    @SerialName("label")
    val label: String,
    @SerialName("nb_tracks")
    val trackCount: Int,
    @SerialName("duration")
    val duration: Long,
    @SerialName("release_date")
    val releaseDate: String,
    @SerialName("explicit_lyrics")
    val isExplicit: Boolean,
    @SerialName("contributors")
    val contributors: List<DeezerArtist>,
    @SerialName("tracks")
    val tracks: DeezerTracksWrapper
)

@Serializable
data class DeezerTracksWrapper(
    @SerialName("data")
    val data: List<DeezerChartTrack>
)