package com.musicapp.ui.models

import android.net.Uri
import androidx.core.net.toUri
import com.musicapp.data.remote.deezer.DeezerArtist

data class ArtistModel(
    val id: Long,
    val name: String,
    val smallPicture: Uri? = null,
    val mediumPicture: Uri? = null,
    val bigPicture: Uri? = null
)

fun DeezerArtist.toModel(): ArtistModel {
    return ArtistModel(
        id = id,
        name = name,
        smallPicture = smallPicture.toUri(),
        mediumPicture = mediumPicture.toUri(),
        bigPicture = bigPicture.toUri()
    )
}