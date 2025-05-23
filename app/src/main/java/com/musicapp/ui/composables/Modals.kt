package com.musicapp.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.musicapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCreationModal(
    showBottomSheet: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) playlistName = ""
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = null,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.new_playlist)) },
                        navigationIcon = {
                            TextButton(onClick = onDismiss) {
                                Text(stringResource(R.string.cancel))
                            }
                        },
                        windowInsets = WindowInsets(0),
                        actions = {
                            TextButton(
                                onClick = {
                                    onCreatePlaylist(playlistName)
                                    onDismiss()
                                },
                                enabled = playlistName.isNotBlank()
                            ) {
                                Text(stringResource(R.string.create))
                            }
                        }
                    )
                }
            ) { contentPadding ->
                Column(
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text(stringResource(R.string.playlist_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}