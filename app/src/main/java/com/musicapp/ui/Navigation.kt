package com.musicapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.musicapp.ui.screens.login.LoginScreen
import com.musicapp.ui.screens.MainScreen
import com.musicapp.ui.screens.passwordrecovery.PasswordRecoveryScreen
import com.musicapp.ui.screens.profile.ProfileScreen
import com.musicapp.ui.screens.settings.SettingsScreen
import com.musicapp.ui.screens.signup.SignUpScreen
import kotlinx.serialization.Serializable

sealed interface MusicAppRoute {
    @Serializable data object Login: MusicAppRoute
    @Serializable data object SignUp: MusicAppRoute
    @Serializable data object Main: MusicAppRoute
    @Serializable data object Settings: MusicAppRoute
    @Serializable data object Profile: MusicAppRoute
    @Serializable data object PasswordRecovery: MusicAppRoute
}

@Composable
fun MusicAppNavGraph(navController: NavHostController, musicAppRoute: MusicAppRoute) {
    NavHost(
        navController = navController,
        startDestination = musicAppRoute
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
        composable<MusicAppRoute.Settings> {
            SettingsScreen(navController)
        }
        composable<MusicAppRoute.Profile> {
            ProfileScreen(navController)
        }
        composable<MusicAppRoute.PasswordRecovery> {
            PasswordRecoveryScreen(navController)
        }
    }
}