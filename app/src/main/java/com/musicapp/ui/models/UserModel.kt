package com.musicapp.ui.models

import com.musicapp.data.database.User

data class UserModel(
    val userId: String,
    val username: String,
    val email: String,
)

fun User.toModel(): UserModel {
    return UserModel(
        userId = userId,
        username = username,
        email = email
    )
}