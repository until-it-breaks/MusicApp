package com.musicapp.ui.screens.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(navController: NavController) {
    val viewModel: ProfileScreenViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    val usernameToDisplay = uiState.currentUser?.username ?: stringResource(R.string.no_username)
    var cameraOutputUri: Uri? by remember { mutableStateOf(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                cameraOutputUri?.let { uri ->
                    viewModel.updateProfilePicture(uri)
                }
            } else {
                Log.e("ProfileScreen", "Photo capture cancelled or failed.")
            }
            viewModel.dismissProfilePictureOptions()
        }
    )

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            viewModel.onCameraPermissionResult(isGranted)
        }
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: SecurityException) {
                    Log.e("ProfileScreen", "Failed to take persistable URI permission for gallery image", e)
                }
                viewModel.updateProfilePicture(uri)
            }
            viewModel.dismissProfilePictureOptions()
        }
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileUiEvent.LaunchCamera -> {
                    cameraOutputUri = event.uri
                    takePictureLauncher.launch(event.uri)
                }
                is ProfileUiEvent.RequestCameraPermission -> {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

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
            LoadableImage(
                imageUri = uiState.currentProfilePictureUri,
                contentDescription = "User Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .padding(0.dp)
                    .clickable { } //TODO show full picture?
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
                // change password
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(MusicAppRoute.PasswordRecovery) }
                ) {
                    Text(stringResource(R.string.change_password))
                }
                Spacer(modifier = Modifier.width(24.dp))

                // delete account
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
                // log out
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
                            viewModel.onTakePhotoClicked()
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
                                viewModel.dismissProfilePictureOptions()
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