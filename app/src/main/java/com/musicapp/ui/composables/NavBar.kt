package com.musicapp.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable

import androidx.compose.material3.Icon
import androidx.compose.material3.Text

@Composable
fun NavBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf("Home", "Search", "Library")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Search, Icons.AutoMirrored.Filled.LibraryBooks)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.Search, Icons.AutoMirrored.Outlined.LibraryBooks)

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                        contentDescription = item
                    )
                },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                label = { Text(item) },
            )
        }
    }
}