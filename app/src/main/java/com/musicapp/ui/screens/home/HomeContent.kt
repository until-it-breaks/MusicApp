package com.musicapp.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeContent() {
    val homeViewModel = remember { HomeViewModel() }
    val username = homeViewModel.username.collectAsState().value
    val loading = homeViewModel.loading.collectAsState().value
    val errorMessage = homeViewModel.errorMessage.collectAsState().value

    val randomMixes = (1..8).map { "Random mix #$it" }

    Column {
        if (loading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(text = "Error: $errorMessage")
        } else {
            Text(text = "Hello ${username ?: "User"}!")
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(randomMixes) { playlist ->
                MixItem(
                    playlist,
                    onClick = { /*TODO*/ })
            }
        }
    }
}

@Composable
fun MixItem(item: String, onClick: () -> Unit) {
    Card(
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
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