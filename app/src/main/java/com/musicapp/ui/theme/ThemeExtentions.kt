package com.musicapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// customColors
val CustomPlayingGreenLight = Color(0xFF4CAF50)
val CustomPlayingGreenDark = Color(0xFF81C784)

val ColorScheme.playingColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) CustomPlayingGreenDark else CustomPlayingGreenLight