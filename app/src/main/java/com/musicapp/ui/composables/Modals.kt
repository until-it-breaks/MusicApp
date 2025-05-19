package com.musicapp.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCreationModal(
    showBottomSheet: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = null
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("New Playlist") },
                        navigationIcon = {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
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
                                Text("Create")
                            }
                        }
                    )
                }
            ) { contentPadding ->
                Column(
                    modifier = Modifier.padding(contentPadding).padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Playlist name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}