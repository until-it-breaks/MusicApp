package com.musicapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.musicapp.ui.composables.MainAppBar
import com.musicapp.ui.composables.NavBar

@Composable
fun MainScreen(navController: NavController) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val categories = listOf("Home", "Search", "Library")
    val currentTitle = categories[selectedItem]

    val randomMixes = (1..8).map { "Random mix #$it" }
    val genres = (1..8).map { "Genre #$it" }
    val playlists = (1..8).map { "Playlist #$it" }

    Scaffold(
        topBar = { MainAppBar(navController, title = currentTitle) },
        bottomBar = {
            NavBar(
                selectedItem = selectedItem,
                onItemSelected = { index -> selectedItem = index })
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
        ) {
            if (categories[selectedItem] == "Home") {
                Text("Hello {Username Placeholder}!")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(contentPadding)
                ) {
                    items(randomMixes) { playlist ->
                        GenericItem(
                            playlist,
                            onClick = { /*TODO*/ })
                    }
                }
            }
            else if (categories[selectedItem] == "Search") {
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    trailingIcon = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Outlined.Search, "Search")
                        }
                    }
                )
                Spacer(Modifier.size(8.dp))
                Text("Discover something new")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(contentPadding)
                ) {
                    items(genres) { genre ->
                        GenericItem(
                            genre,
                            onClick = { /*TODO*/ })
                    }
                }
            }
            else if (categories[selectedItem] == "Library") {
                Text("Your playlists")
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(contentPadding)
                ) {
                    items(playlists) { playlist ->
                        GenericItem(
                            playlist,
                            onClick = { /*TODO*/ })
                    }
                }
            }
            /*
            else if (categories[selectedItem] == "Profile") {
                Row {
                    Image(
                        Icons.Outlined.Image,
                        "Profile picture",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .size(64.dp)
                            .clip(CircleShape)
                            .padding(36.dp)
                    )
                    Column {
                        Text("Profile Name")
                        Text("View Profile")
                    }
                }
            }
             */
        }
    }
}

@Composable
fun GenericItem(item: String, onClick: () -> Unit) {
    Card(
        onClick = onClick
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                Icons.Outlined.Image,
                "Item Picture",
                modifier = Modifier.size(48.dp)
                    .padding(end = 8.dp)
            )
            Text(item)
        }
    }
}