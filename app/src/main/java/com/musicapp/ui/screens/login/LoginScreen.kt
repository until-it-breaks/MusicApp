package com.musicapp.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.data.util.OperationState
import com.musicapp.ui.MusicAppRoute
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(navController: NavController) {
    val loginViewModel = koinViewModel<LoginViewModel>()
    val loginState by loginViewModel.loginState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        focusManager.clearFocus()
                    }
                }
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
            ) {
                Image(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = stringResource(R.string.app_logo_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email_label)) },
                modifier = Modifier.width(300.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password_label)) },
                modifier = Modifier.width(300.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    focusManager.clearFocus()
                    loginViewModel.login(email, password)
                }),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) {
                        stringResource(R.string.hide_password)
                    } else {
                        stringResource(R.string.show_password)
                    }
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )
            TextButton(
                onClick = { navController.navigate(MusicAppRoute.PasswordRecovery) },
            ) {
                Text(stringResource(R.string.forgot_password))
            }
            Button(
                onClick = {
                    focusManager.clearFocus()
                    loginViewModel.login(email, password)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = email.isNotBlank() && password.isNotBlank()
            ) {
                Text(stringResource(R.string.login))
            }

            when (loginState) {
                is OperationState.Ongoing -> {
                    Spacer(modifier = Modifier.weight(0.25f))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.weight(0.25f))
                }
                is OperationState.Success -> {
                    LaunchedEffect(loginState) {
                        navController.navigate(MusicAppRoute.Main)
                    }
                }
                is OperationState.Error -> {
                    LaunchedEffect(loginState) {
                        val errorState = loginState as OperationState.Error
                        val errorMessage = errorState.stringKey?.let { context.getString(it) } ?: errorState.message
                        val result = snackbarHostState.showSnackbar(
                            message = errorMessage,
                            duration = SnackbarDuration.Long,
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.Dismissed) {
                            loginViewModel.resetState()
                        }
                    }
                }
                is OperationState.Idle -> {
                    /* CircularProgressIndicator() */
                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }

            TextButton (onClick = { navController.navigate(MusicAppRoute.SignUp) }) {
                Text(stringResource(R.string.sign_up_now))
            }
            Spacer(modifier = Modifier.weight(2f))
        }
    }
}