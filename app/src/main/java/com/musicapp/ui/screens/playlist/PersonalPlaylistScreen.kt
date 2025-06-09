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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.EditPlaylistNameModal
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.SavedTrackDropDownMenu
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.composables.TrackCard
import com.musicapp.ui.composables.UserPlaylistDropDownMenu
import com.musicapp.ui.theme.AppPadding
import com.musicapp.util.convertMillisToDateWithHourAndMinutes
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalPlaylistScreen(navController: NavController, playlistId: String) {
    val viewModel = koinViewModel<PersonalPlaylistViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playlist = viewModel.playlist.collectAsStateWithLifecycle()
    val playbackUiState by viewModel.playbackUiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

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
                .padding(AppPadding.ScaffoldContent)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LoadableImage(
                        imageUri = playlist.value?.playlistPictureUri,
                        contentDescription = null,
                        modifier = Modifier.size(250.dp)
                    )
                    val timeInMillis = playlist.value?.lastEditTime
                    timeInMillis?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${stringResource(R.string.last_edited)}: ${convertMillisToDateWithHourAndMinutes(it)}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            itemsIndexed(playlist.value?.tracks.orEmpty()) { index, track ->
                TrackCard(
                    track = track,
                    showPicture = true,
                    playbackUiState = playbackUiState,
                    onTrackClick = {scope.launch { viewModel.setPlaybackQueue(playlist.value?.tracks.orEmpty(), index) } },
                    onArtistClick = { artistId -> navController.navigate(MusicAppRoute.Artist(artistId)) },
                    extraMenu = {
                        SavedTrackDropDownMenu(
                            track = track,
                            onAddToQueue = viewModel::addTrackToQueue,
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