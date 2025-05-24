package com.musicapp.ui.screens.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.EditPlaylistNameModal
import com.musicapp.ui.composables.SavedTrackDropDownMenu
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TrackCard
import com.musicapp.ui.composables.UserPlaylistDropDownMenu
import com.musicapp.util.convertMillisToDateWithHourAndMinutes
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalPlaylistScreen(navController: NavController, playlistId: String) {
    val viewModel = koinViewModel<PersonalPlaylistViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playlist = viewModel.playlist.collectAsStateWithLifecycle()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistTracks(playlistId)
    }

    LaunchedEffect(uiState.deletionSuccessful) {
        if (uiState.deletionSuccessful) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                navController = navController,
                title = playlist.value?.name ?: stringResource(R.string.unknown_playlist),
                content = {
                    playlist.value?.let {
                        UserPlaylistDropDownMenu(
                            onDeletePlaylist = { viewModel.deletePlaylist() },
                            onEditPlaylistName = { viewModel.startEditingName(it.name) }
                        )
                    }
                }
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(128.dp)
                    )
                    val timeInMillis = playlist.value?.lastEditTime
                    timeInMillis?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${stringResource(R.string.last_edited)}: ${convertMillisToDateWithHourAndMinutes(it)}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            items(playlist.value?.tracks.orEmpty()) { track ->
                TrackCard(
                    track = track,
                    showPicture = true,
                    onTrackClick = { viewModel.playTrack(track) },
                    onArtistClick = { artistId -> navController.navigate(MusicAppRoute.Artist(artistId)) },
                    extraMenu = {
                        SavedTrackDropDownMenu(
                            track = track,
                            onAddToQueue = { viewModel.addToQueue(track) },
                            onRemoveTrack = { viewModel.removeTrackFromPlaylist(track.id) }
                        )
                    }
                )
            }
        }
    }

    if (uiState.isEditingName && playlist.value != null) {
        val sheetState = rememberModalBottomSheetState()
        EditPlaylistNameModal(
            sheetState = sheetState,
            currentName = uiState.newName,
            onNameChange = viewModel::onPlaylistNameChanged,
            canSubmit = uiState.canSubmitNameChange,
            onConfirm = viewModel::confirmNameChange,
            onDismiss = viewModel::dismissEditingName
        )
    }
}