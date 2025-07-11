package com.musicapp.data.models

import android.net.Uri
import androidx.core.net.toUri
import com.musicapp.data.database.User

data class UserModel(
    val userId: String,
    val username: String,
    val email: String,
    val profilePictureUri: Uri = Uri.EMPTY
)

fun User.toModel(): UserModel {
    return UserModel(
        userId = userId,
        username = username,
        email = email,
        profilePictureUri = profilePictureUri?.toUri() ?: Uri.EMPTY
    )
}