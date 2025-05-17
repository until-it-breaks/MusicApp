package com.musicapp.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.PlaylistCreationModal
import com.musicapp.ui.composables.MainTopBar
import com.musicapp.ui.composables.UserPlaylistCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(mainNavController: NavController, subNavController: NavController) {
    val viewModel = koinViewModel<LibraryViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playlists = viewModel.playlists.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MainTopBar(mainNavController, "Library") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true }
            ) {
                Icon(Icons.Filled.Add, "Create new playlist")
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        if (!uiState.showAuthError) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(contentPadding).padding(12.dp)
            ) {
                item {
                    if (uiState.likedTracksPlaylist != null) {
                        UserPlaylistCard(
                            title = "Liked songs",
                            onClick = { subNavController.navigate(MusicAppRoute.LikedSongs) }
                        )
                    }
                }
                item {
                    if (uiState.trackHistory != null) {
                        UserPlaylistCard(
                            title = "Track history",
                            onClick = { subNavController.navigate(MusicAppRoute.TrackHistory) }
                        )
                    }
                }
                item {
                    Text(
                        text = "Your playlists",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (playlists.value.isEmpty()) {
                    item {
                        Text("No playlists founds")
                    }
                } else {
                    items(playlists.value) { playlist ->
                        UserPlaylistCard(
                            title = playlist.name,
                            onClick = { subNavController.navigate(MusicAppRoute.UserPlaylist(playlist.id)) }
                        )
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Failed to authenticate. Please login again")
                Button(
                    onClick = {
                        mainNavController.navigate(MusicAppRoute.Login) {
                            popUpTo(mainNavController.graph.id) { inclusive = true }
                        }
                    }
                ) {
                    Text("Go to login")
                }
            }
        }
    }
    PlaylistCreationModal(
        showBottomSheet = showBottomSheet,
        sheetState = sheetState,
        onDismiss = { showBottomSheet = false },
        onCreatePlaylist = { name -> viewModel.createPlaylist(name) }
    )
}