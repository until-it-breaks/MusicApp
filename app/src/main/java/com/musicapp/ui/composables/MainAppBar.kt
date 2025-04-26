package com.musicapp.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.musicapp.ui.MusicAppRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(navController: NavController, title: String) {
    TopAppBar(
        title = {
            Text(title)
        },
        actions = {
                IconButton(onClick = { navController.navigate(MusicAppRoute.Profile)}) {
                    Icon(Icons.Outlined.Person, contentDescription = "Profile")
                }
        }
    )
}