package com.musicapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.musicapp.ui.screens.LoginScreen
import com.musicapp.ui.screens.MainScreen
import com.musicapp.ui.screens.RegisterScreen
import kotlinx.serialization.Serializable

sealed interface MusicAppRoute {
    @Serializable data object Login: MusicAppRoute
    @Serializable data object Register: MusicAppRoute
    @Serializable data object Main: MusicAppRoute
}

@Composable
fun MusicAppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = MusicAppRoute.Login
    ) {
        composable<MusicAppRoute.Login> {
            LoginScreen(navController)
        }
        composable<MusicAppRoute.Register> {
            RegisterScreen(navController)
        }
        composable<MusicAppRoute.Main> {
            MainScreen(navController)
        }
    }
}