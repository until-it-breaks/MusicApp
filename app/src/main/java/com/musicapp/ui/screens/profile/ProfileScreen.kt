package com.musicapp.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(navController: NavController) {
    val viewModel: ProfileScreenViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState().value

    val usernameToDisplay = uiState.currentUser?.username ?: stringResource(R.string.no_username)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                /*TODO*/
            }
            viewModel.dismissProfilePictureOptions()
        }
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                viewModel.updateProfilePicture(uri)
            }
            viewModel.dismissProfilePictureOptions()
        }
    )

    Scaffold(
        topBar = { TopBarWithBackButton(navController, title = stringResource(R.string.profile)) }
    ) { contentPadding ->

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // user Image
            Image(
                painter = if (uiState.currentProfilePictureUri != null && uiState.currentProfilePictureUri != Uri.EMPTY) {
                    rememberAsyncImagePainter(uiState.currentProfilePictureUri)
                } else {
                    rememberAsyncImagePainter(model = uiState.currentProfilePictureUri)
                },
                contentDescription = "User image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(if (uiState.currentProfilePictureUri == null || uiState.currentProfilePictureUri == Uri.EMPTY) 36.dp else 0.dp)
            )
            TextButton(onClick = { viewModel.showProfilePictureOptions() }) {
                Text("Edit photo")
            }
            // Username
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.username),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp)
                )
                Card(
                    onClick = { viewModel.showUsernameDialog() },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = usernameToDisplay,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO */ }
                ) {
                    Text(stringResource(R.string.change_password))
                }
                Spacer(modifier = Modifier.width(24.dp))
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.delete_profile))
                }
                Spacer(modifier = Modifier.width(24.dp))
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.logout()
                        navController.navigate(MusicAppRoute.Login) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            } // Prevents going back to this screen
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Logout")
                }
            }
        }

        // change name dialog
        if (uiState.showChangeUsernameDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUsernameDialog() },
                title = { Text(text = stringResource(R.string.change_username)) },
                text = {
                    OutlinedTextField(
                        value = uiState.newUsernameInput,
                        onValueChange = { viewModel.onNewUsernameChanged(it) },
                        label = { Text(stringResource(R.string.new_username)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.updateUsername() }) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUsernameDialog() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
        // change profile picture
        if (uiState.showProfilePictureOptions) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissProfilePictureOptions() },
                title = { Text(text = stringResource(R.string.edit_photo)) },
                text = {
                    Column {
                        TextButton(onClick = {
                            /*TODO*/
                        }) {
                            Text(stringResource(R.string.take_photo_now))
                        }
                        TextButton(onClick = {
                            pickImageLauncher.launch("image/*")
                        }) {
                            Text(stringResource(R.string.choose_from_gallery))
                        }
                        if (!uiState.isDefaultProfilePicture) {
                            TextButton(onClick = {
                                viewModel.removeProfilePicture()
                            }) {
                                Text(stringResource(R.string.remove_current_photo))
                            }
                        }
                    }
                },
                confirmButton = { }
            )
        }
    }
}