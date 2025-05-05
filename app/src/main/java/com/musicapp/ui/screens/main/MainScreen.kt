package com.musicapp.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.musicapp.ui.screens.main.home.HomeContent
import com.musicapp.ui.screens.main.library.LibraryContent
import com.musicapp.ui.composables.MainTopBar
import com.musicapp.ui.composables.MainNavBar
import com.musicapp.ui.screens.main.search.SearchContent

@Composable
fun MainScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf(MainCategory.HOME) }

    Scaffold(
        topBar = { MainTopBar(navController, title = stringResource(selectedCategory.stringId)) },
        bottomBar = {
            MainNavBar(
                items = MainCategory.entries.toList(),
                selectedItem = selectedCategory,
                onItemSelected = { category -> selectedCategory = category })
        }
    ) { contentPadding ->
        val modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
        when (selectedCategory) {
            MainCategory.HOME -> HomeContent(modifier)
            MainCategory.SEARCH -> SearchContent(modifier)
            MainCategory.LIBRARY -> LibraryContent(modifier)
        }
    }
}
