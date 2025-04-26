package com.musicapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.musicapp.ui.screens.LoginScreen
import com.musicapp.ui.screens.MainScreen
import com.musicapp.ui.screens.ProfileScreen
import com.musicapp.ui.screens.SignUpScreen
import kotlinx.serialization.Serializable

sealed interface MusicAppRoute {
    @Serializable object Login: MusicAppRoute
    @Serializable object SignUp: MusicAppRoute
    @Serializable object Main: MusicAppRoute
    @Serializable object Profile: MusicAppRoute
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
        composable<MusicAppRoute.SignUp> {
            SignUpScreen(navController)
        }
        composable<MusicAppRoute.Main> {
            MainScreen(navController)
        }
        composable<MusicAppRoute.Profile> {
            ProfileScreen(navController)
        }
    }
}