package com.musicapp.data.models

import android.net.Uri
import androidx.core.net.toUri
import com.musicapp.data.database.Artist
import com.musicapp.data.remote.deezer.DeezerArtist

data class ArtistModel(
    val id: Long,
    val name: String,
    val smallPictureUri: Uri = Uri.EMPTY,
    val mediumPictureUri: Uri = Uri.EMPTY,
    val bigPictureUri: Uri = Uri.EMPTY
)

fun DeezerArtist.toModel(): ArtistModel {
    return ArtistModel(
        id = id,
        name = name,
        smallPictureUri = smallPicture?.toUri() ?: Uri.EMPTY,
        mediumPictureUri = mediumPicture?.toUri() ?: Uri.EMPTY,
        bigPictureUri = bigPicture?.toUri() ?: Uri.EMPTY
    )
}

fun Artist.toModel(): ArtistModel {
    return ArtistModel(
        id = artistId,
        name = name,
        smallPictureUri = smallPictureUri?.toUri() ?: Uri.EMPTY,
        mediumPictureUri = mediumPictureUri?.toUri() ?: Uri.EMPTY,
        bigPictureUri = bigPictureUri?.toUri() ?: Uri.EMPTY,
    )
}

fun ArtistModel.toDbEntity(): Artist {
    return Artist(
        artistId = id,
        name = name,
        smallPictureUri = smallPictureUri.toString(),
        mediumPictureUri = mediumPictureUri.toString(),
        bigPictureUri = bigPictureUri.toString(),
    )
}