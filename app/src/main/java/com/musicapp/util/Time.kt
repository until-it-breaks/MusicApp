package com.musicapp.util

fun convertDurationInSecondsToString(seconds: Long): String {
    if (seconds > 3600) {
        val hours = seconds / 3600
        val minutes = seconds % 3600 / 60
        return "${hours}h${minutes}min"
    } else {
        return "${seconds / 60}min"
    }
}