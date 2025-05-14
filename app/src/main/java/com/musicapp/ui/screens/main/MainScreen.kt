package com.musicapp.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.composables.MainTopBar
import com.musicapp.ui.composables.MainNavBar
import com.musicapp.ui.screens.home.HomeScreen
import com.musicapp.ui.screens.library.LibraryScreen
import com.musicapp.ui.screens.search.SearchScreen

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

@Composable
fun MainScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf(MainCategory.HOME) }

    Scaffold(
        topBar = { MainTopBar(navController, title = stringResource(selectedCategory.stringId)) },
        bottomBar = {
            // Put the music player bar here
            MainNavBar( // Bottom nav bar
                items = MainCategory.entries.toList(),
                selectedItem = selectedCategory,
                onItemSelected = { category -> selectedCategory = category }
            )
        }
    ) { contentPadding ->
        val modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
        when (selectedCategory) {
            MainCategory.HOME -> HomeScreen(navController, modifier)
            MainCategory.SEARCH -> SearchScreen(modifier)
            MainCategory.LIBRARY -> LibraryScreen(navController, modifier)
        }
    }
}
