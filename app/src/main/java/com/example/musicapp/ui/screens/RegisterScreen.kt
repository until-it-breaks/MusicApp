package com.example.musicapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RegisterScreen(navController: NavController) {
    Scaffold { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Row (
                modifier = Modifier.height(50.dp),

            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Sign Up",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text(
                        "Discover new music",
                        style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Image(
                    Icons.Outlined.MusicNote,
                    "App Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp)
                )
            }


            var email by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }
            var username by rememberSaveable { mutableStateOf("") }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label ={ Text("Email") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label ={ Text("Password") }
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label ={ Text("Username") }
            )
            Button(
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                onClick = { },
            ) {
                Text("Create an account")
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}