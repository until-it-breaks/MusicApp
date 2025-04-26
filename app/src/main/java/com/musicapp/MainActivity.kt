package com.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.ui.MusicAppNavGraph
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.theme.MusicAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicAppTheme {
                val navController = rememberNavController()
                val auth = remember { FirebaseAuth.getInstance() }
                val route = remember(auth.currentUser) {
                    if (auth.currentUser != null) {
                        MusicAppRoute.Main
                    } else {
                        MusicAppRoute.Login
                    }
                }
                MusicAppNavGraph(navController, route)
            }
        }
    }
}
