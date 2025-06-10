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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.AuthErrorMessage
import com.musicapp.ui.composables.MainTopBar
import com.musicapp.ui.composables.PlaylistCreationModal
import com.musicapp.ui.composables.PlaylistType
import com.musicapp.ui.composables.UserPlaylistCard
import com.musicapp.ui.theme.AppPadding
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(mainNavController: NavController, subNavController: NavController) {
    val viewModel = koinViewModel<LibraryViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MainTopBar(mainNavController, stringResource(R.string.library)) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null
                )
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        if (!uiState.showAuthError) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(AppPadding.ScaffoldContent)
            ) {
                item {
                    UserPlaylistCard(
                        title = stringResource(R.string.liked_tracks),
                        playlistType = PlaylistType.LIKED,
                        onClick = { subNavController.navigate(MusicAppRoute.LikedSongs) }
                    )
                }
                item {
                    UserPlaylistCard(
                        title = stringResource(R.string.track_history),
                        playlistType = PlaylistType.HISTORY,
                        onClick = { subNavController.navigate(MusicAppRoute.TrackHistory) }
                    )
                }
                item {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.your_playlists),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        if (playlists.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_playlists_found),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                items(playlists) { playlist ->
                    UserPlaylistCard(
                        title = playlist.name,
                        imageUri = playlist.playlistPictureUri,
                        onClick = { subNavController.navigate(MusicAppRoute.UserPlaylist(playlist.id)) }
                    )
                }
            }
            PlaylistCreationModal(
                showBottomSheet = showBottomSheet,
                sheetState = sheetState,
                onDismiss = { showBottomSheet = false },
                onCreatePlaylist = { name -> viewModel.createPlaylist(name) }
            )
        } else {
            AuthErrorMessage(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(AppPadding.ScaffoldContent)
                    .fillMaxWidth(),
                onClick = {
                    viewModel.logout()
                    mainNavController.navigate(MusicAppRoute.Login) {
                        popUpTo(mainNavController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}