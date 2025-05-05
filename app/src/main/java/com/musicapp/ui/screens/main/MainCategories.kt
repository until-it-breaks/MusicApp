package com.musicapp.ui.screens.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.musicapp.R

enum class MainCategory(val stringId: Int, val primaryIcon: ImageVector, val secondaryIcon: ImageVector) {
    HOME(
        R.string.home_screen_name,
        Icons.Filled.Home,
        Icons.Outlined.Home
    ),
    SEARCH(
        R.string.search_screen_name,
        Icons.Filled.Search,
        Icons.Outlined.Search
    ),
    LIBRARY(
        R.string.library_screen_name,
        Icons.AutoMirrored.Filled.LibraryBooks,
        Icons.AutoMirrored.Outlined.LibraryBooks
    )
}