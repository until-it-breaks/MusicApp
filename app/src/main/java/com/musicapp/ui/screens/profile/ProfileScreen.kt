package com.musicapp.ui.screens.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.LoadableImage
import com.musicapp.ui.composables.TopBarWithBackButton
import com.musicapp.ui.theme.AppPadding
import org.koin.androidx.compose.koinViewModel

private const val TAG = "ProfileScreen"

@Composable
fun ProfileScreen(navController: NavController) {
    val viewModel: ProfileScreenViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val user = viewModel.currentUser.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var cameraOutputUri: Uri? by remember { mutableStateOf(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                cameraOutputUri?.let { uri ->
                    viewModel.updateProfilePicture(uri)
                }
            } else {
                Log.e(TAG, "Photo capture cancelled or failed.")
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
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Log.e(TAG, "Failed to take persistable URI permission for gallery image", e)
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
        topBar = { TopBarWithBackButton(navController, title = stringResource(R.string.profile)) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { navController.navigate(MusicAppRoute.PasswordRecovery) }
                ) {
                    Text(
                        text = stringResource(R.string.change_password),
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { viewModel.showConfirmDelete() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = stringResource(R.string.delete_profile),
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { viewModel.showConfirmLogout() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = stringResource(R.string.logout),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(AppPadding.ScaffoldContent)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            LoadableImage(
                imageUri = user.value?.profilePictureUri?.toUri(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
            )
            TextButton(onClick = { viewModel.showProfilePictureOptions() }) {
                Text(stringResource(R.string.edit_photo))
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.username),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 12.dp)
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
                            text = user.value?.username ?: stringResource(R.string.unknown_user),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.email),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Card {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = user.value?.email ?: stringResource(R.string.unknown_email),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }

        if (uiState.value.showChangeUsernameDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUsernameDialog() },
                title = { Text(text = stringResource(R.string.change_username)) },
                text = {
                    OutlinedTextField(
                        value = uiState.value.newUsernameInput,
                        onValueChange = { viewModel.onNewUsernameChanged(it) },
                        label = { Text(stringResource(R.string.new_username)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = uiState.value.canChangeName,
                        onClick = {
                            viewModel.updateUsername()
                            viewModel.dismissUsernameDialog()
                        }
                    ) {
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

        if (uiState.value.showProfilePictureOptions) {
            Dialog(onDismissRequest = { viewModel.dismissProfilePictureOptions() }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .wrapContentWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.edit_photo),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        TextButton(onClick = { viewModel.onTakePhotoClicked() }) {
                            Text(text = stringResource(R.string.take_photo_now))
                        }
                        TextButton(onClick = {
                            pickImageLauncher.launch("image/*")
                        }) {
                            Text(text = stringResource(R.string.choose_from_gallery))
                        }
                        if (user.value?.profilePictureUri != null) {
                            TextButton(onClick = {
                                viewModel.removeProfilePicture()
                                viewModel.dismissProfilePictureOptions()
                            }) {
                                Text(text = stringResource(R.string.remove_current_photo))
                            }
                        }
                    }
                }
            }
        }

        if (uiState.value.showConfirmDelete) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissConfirmDelete() },
                title = { Text(text = stringResource(R.string.confirm_delete_account)) },
                text = { Text(text = stringResource(R.string.delete_account_warning)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.dismissConfirmDelete()
                        viewModel.deleteAccount()
                        navController.navigate(MusicAppRoute.Login) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissConfirmDelete() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        if (uiState.value.showConfirmLogout) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissConfirmLogout() },
                title = { Text(text = stringResource(R.string.logout)) },
                text = { Text(text = stringResource(R.string.are_you_sure_you_want_to_logout)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.dismissConfirmLogout()
                        viewModel.logout()
                        navController.navigate(MusicAppRoute.Login) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissConfirmLogout() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}