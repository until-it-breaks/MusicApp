package com.musicapp.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.musicapp.ui.MusicAppRoute

@Composable
fun LoginScreen(navController: NavController) {
    val loginViewModel: LoginViewModel = viewModel()
    val loginUiState by loginViewModel.loginState.collectAsState()

    Scaffold { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {
                Image(
                    Icons.Outlined.MusicNote,
                    "App Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "MusicApp",
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            var email by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") }
            )
            TextButton(
                onClick = { /*TODO*/ },
            ) {
                Text("Forgot your password?")
            }
            Button(
                onClick = { loginViewModel.login(email, password) }
            ) {
                Text("Log in")
            }
            Spacer(modifier = Modifier.height(36.dp))
            TextButton (onClick = { navController.navigate(MusicAppRoute.SignUp) }) {
                Text("Don't have an account? Sign up")
            }
            Spacer(modifier = Modifier.weight(0.5f))

            when (loginUiState) {
                is LoginViewModel.LoginState.Loading -> {
                    CircularProgressIndicator()
                }
                is LoginViewModel.LoginState.Success -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(MusicAppRoute.Main)
                    }
                }
                is LoginViewModel.LoginState.Error -> {
                    val errorMessage = (loginUiState as LoginViewModel.LoginState.Error).errorMessage
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }
                is LoginViewModel.LoginState.Idle -> {

                }
            }
        }
    }
}
