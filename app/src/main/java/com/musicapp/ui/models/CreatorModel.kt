package com.musicapp.ui.models

import com.musicapp.data.remote.deezer.DeezerCreator

data class CreatorModel(
    val id: Long,
    val name: String
)

fun DeezerCreator.toModel(): CreatorModel {
    return CreatorModel(
        id = id,
        name = name
    )
}