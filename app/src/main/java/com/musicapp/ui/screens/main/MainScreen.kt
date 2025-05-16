package com.musicapp.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.musicapp.R
import com.musicapp.ui.HomeNavGraph
import com.musicapp.ui.LibraryNavGraph
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.SearchNavGraph
import com.musicapp.ui.composables.MainNavBar

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

    val homeNavController = rememberNavController()
    val searchNavController = rememberNavController()
    val libraryNavController = rememberNavController()

    Column(modifier = Modifier.fillMaxSize()) {
        // Music player bar goes --> HERE <--
        Row(
            modifier = Modifier.weight(1f)
        ) {
            when (selectedCategory) {
                MainCategory.HOME -> HomeNavGraph(navController, homeNavController)
                MainCategory.SEARCH -> SearchNavGraph(navController, searchNavController)
                MainCategory.LIBRARY -> LibraryNavGraph(navController, libraryNavController)
            }
        }
        MainNavBar(
            categories = MainCategory.entries.toList(),
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                if (selectedCategory == category) {
                    when (category) {
                        MainCategory.HOME -> {
                            homeNavController.popBackStack(
                                route = MusicAppRoute.Home,
                                inclusive = false
                            )
                        }
                        MainCategory.SEARCH -> {
                            searchNavController.popBackStack(
                                route = MusicAppRoute.Search,
                                inclusive = false
                            )
                        }
                        MainCategory.LIBRARY -> {
                            libraryNavController.popBackStack(
                                route = MusicAppRoute.Library,
                                inclusive = false
                            )
                        }
                    }
                } else {
                    selectedCategory = category
                }
            }
        )
    }
}
