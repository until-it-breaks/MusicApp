package com.musicapp.ui.screens.login

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.musicapp.data.util.OperationState
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun PasswordRecoveryScreen(navController: NavController) {
    val passwordRecoveryViewModel = koinViewModel<PasswordRecoveryViewModel>()
    val recoveryProcessState by passwordRecoveryViewModel.recoveryProcessState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopBarWithBackButton("Password Recovery", navController) },
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
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        focusManager.clearFocus()
                    }
                }
        ) {
            Text(
                text = "Enter your email address below and we'll send you instructions to reset your password.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.width(300.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { focusManager.clearFocus() })
            )
            Button(
                onClick = {
                    focusManager.clearFocus()
                    passwordRecoveryViewModel.sendPasswordResetEmail(email)
                },
                enabled = email.isNotBlank()
            ) {
                Text("Send Reset Email")
            }
            when(recoveryProcessState) {
                is OperationState.Ongoing -> CircularProgressIndicator()
                is OperationState.Success -> {
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar(
                            message = "Password reset email sent successfully!",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                is OperationState.Error -> {
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar(
                            message = (recoveryProcessState as OperationState.Error).message,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                is OperationState.Idle -> {}
            }
        }
    }
}