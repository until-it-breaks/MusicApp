package com.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.musicapp.ui.MusicAppNavGraph
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.theme.MusicAppTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicAppTheme {
                val navController = rememberNavController()
                val viewModel : MainViewModel = koinViewModel()
                val initialRoute = if (viewModel.isSessionActive()) MusicAppRoute.Main else MusicAppRoute.Login
                MusicAppNavGraph(navController, initialRoute)
            }
        }
    }
}
