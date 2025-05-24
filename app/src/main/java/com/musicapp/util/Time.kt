package com.musicapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertDurationInSecondsToString(seconds: Long): String {
    if (seconds > 3600) {
        val hours = seconds / 3600
        val minutes = seconds % 3600 / 60
        return "${hours}h${minutes}min"
    } else {
        return "${seconds / 60}min"
    }
}

fun convertMillisToDateWithHourAndMinutes(millis: Long): String {
    val format = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
    return format.format(Date(millis))
}