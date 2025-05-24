package com.musicapp.ui.models

import android.net.Uri
import androidx.core.net.toUri
import com.musicapp.data.database.Artist
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

fun Artist.toModel(): ArtistModel {
    return ArtistModel(
        id = artistId,
        name = name,
        smallPicture = smallPictureUri?.toUri(),
        mediumPicture = mediumPictureUri?.toUri(),
        bigPicture = bigPictureUri?.toUri(),
    )
}

fun ArtistModel.toDbEntity(): Artist {
    return Artist(
        artistId = id,
        name = name,
        smallPictureUri = smallPicture.toString(),
        mediumPictureUri = mediumPicture.toString(),
        bigPictureUri = bigPicture.toString(),
    )
}