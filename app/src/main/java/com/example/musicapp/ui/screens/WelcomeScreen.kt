package com.example.musicapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.musicapp.R
import com.example.musicapp.ui.MusicAppRoute

@Composable
fun WelcomeScreen(navController: NavController) {
    Scaffold { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "App Logo"
            )
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo"
            )
            Button(
                onClick = { navController.navigate(MusicAppRoute.Register) },
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text("Sign up for free")
            }
            Button(
                onClick = { navController.navigate(MusicAppRoute.Login) },
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text("Log in")
            }
        }
    }
}