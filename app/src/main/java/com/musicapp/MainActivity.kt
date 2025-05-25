package com.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
<<<<<<< HEAD
import com.musicapp.playback.MediaPlayerManager
=======
import com.musicapp.data.models.Theme
>>>>>>> 04655c0264bcfff0ccbc96c7107dedde98305482
import com.musicapp.ui.MusicAppNavGraph
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.theme.MusicAppTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private val mediaPlayerManager: MediaPlayerManager by inject()
    // destroy the media player when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerManager.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = koinViewModel<MainActivityViewModel>()
            val theme = viewModel.theme.collectAsStateWithLifecycle()

            MusicAppTheme(
                darkTheme = when (theme.value) {
                    Theme.Light -> false
                    Theme.Dark -> true
                    else -> isSystemInDarkTheme()
                }
            ) {
                val navController = rememberNavController()
                val viewModel : MainActivityViewModel = koinViewModel()
                val initialRoute = if (viewModel.isSessionActive()) MusicAppRoute.Main else MusicAppRoute.Login
                MusicAppNavGraph(navController, initialRoute)
            }
        }
    }
}
