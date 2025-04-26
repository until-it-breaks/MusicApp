package com.musicapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.musicapp.ui.screens.home.HomeContent
import com.musicapp.ui.screens.library.LibraryContent
import com.musicapp.ui.composables.MainTopBar
import com.musicapp.ui.composables.NavBar
import com.musicapp.ui.screens.search.SearchContent

@Composable
fun MainScreen(navController: NavController) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val categories = listOf("Home", "Search", "Library")
    val currentTitle = categories[selectedItem]

    Scaffold(
        topBar = { MainTopBar(navController, title = currentTitle) },
        bottomBar = {
            NavBar(
                selectedItem = selectedItem,
                onItemSelected = { index -> selectedItem = index })
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            when (categories[selectedItem]) {
                "Home" -> HomeContent()
                "Search" -> SearchContent()
                "Library" -> LibraryContent()
            }
        }
    }
}
