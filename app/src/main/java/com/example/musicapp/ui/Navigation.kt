package com.example.musicapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.musicapp.ui.screens.LoginScreen
import com.example.musicapp.ui.screens.RegisterScreen
import com.example.musicapp.ui.screens.WelcomeScreen
import kotlinx.serialization.Serializable

sealed interface MusicAppRoute {
    @Serializable data object Welcome: MusicAppRoute
    @Serializable data object Login: MusicAppRoute
    @Serializable data object Register: MusicAppRoute
}

@Composable
fun MusicAppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = MusicAppRoute.Welcome
    ) {
        composable<MusicAppRoute.Welcome> {
            WelcomeScreen(navController)
        }
        composable<MusicAppRoute.Login> {
            LoginScreen(navController)
        }
        composable<MusicAppRoute.Register> {
            RegisterScreen(navController)
        }
    }
}