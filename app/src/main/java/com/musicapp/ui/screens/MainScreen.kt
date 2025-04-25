package com.musicapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.musicapp.ui.composables.AppBar
import com.musicapp.ui.composables.NavBar

@Composable
fun MainScreen(navController: NavController) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Home", "Search", "Library")
    val currentTitle = items[selectedItem]

    Scaffold(
        topBar = { AppBar(navController, title = currentTitle) },
        bottomBar = {
            NavBar(
                selectedItem = selectedItem,
                onItemSelected = { index -> selectedItem = index })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            // TODO
        }
    }
}