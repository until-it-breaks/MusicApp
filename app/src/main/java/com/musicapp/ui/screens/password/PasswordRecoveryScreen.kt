package com.musicapp.ui.screens.password

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun PasswordRecoveryScreen(navController: NavController) {
    val passwordRecoveryViewModel = koinViewModel<PasswordRecoveryViewModel>()
    val state by passwordRecoveryViewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = { TopBarWithBackButton(navController, stringResource(R.string.password_recovery_screen_name)) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .padding(12.dp)
                .imePadding()
                .pointerInput(Unit) { detectTapGestures { offset -> focusManager.clearFocus() } }
        ) {
            Text(
                text = stringResource(R.string.enter_instructions_to_reset_password),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = passwordRecoveryViewModel::onEmailChanged,
                label = { Text(stringResource(R.string.email_label)) },
                modifier = Modifier.width(300.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { focusManager.clearFocus() })
            )
            Button(
                enabled = state.canSubmit,
                onClick = {
                    focusManager.clearFocus()
                    passwordRecoveryViewModel.sendPasswordResetEmail()
                },
            ) {
                Text(stringResource(R.string.send_password_reset_email))
            }
            if (state.isLoading) {
                CircularProgressIndicator()
            }
            if (state.errorMessageId != null) {
                LaunchedEffect(state.errorMessageId) {
                    val errorMessage = context.getString(state.errorMessageId!!)
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Long,
                        withDismissAction = true
                    )
                }
            }
            if (state.emailSent) {
                LaunchedEffect(Unit) {
                    val message = context.getString(R.string.password_reset_email_sent)
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Long,
                        withDismissAction = true
                    )
                }
            }
        }
    }
}