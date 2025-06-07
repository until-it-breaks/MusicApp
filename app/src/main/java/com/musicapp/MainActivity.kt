package com.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.musicapp.data.models.Theme
import com.musicapp.ui.MusicAppNavGraph
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.theme.MusicAppTheme
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.koinViewModel

@UnstableApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoinAndroidContext {
                val viewModel = koinViewModel<MainActivityViewModel>()
                val theme = viewModel.theme.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                val initialRoute = remember {
                    if (viewModel.isSessionActive()) MusicAppRoute.Main else MusicAppRoute.Login
                }

                MusicAppTheme(
                    darkTheme = when (theme.value) {
                        Theme.Light -> false
                        Theme.Dark -> true
                        else -> isSystemInDarkTheme()
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        MusicAppNavGraph(navController, initialRoute)
                    }
                }
            }
        }
    }
}
