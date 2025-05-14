package com.musicapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.musicapp.ui.screens.login.LoginScreen
import com.musicapp.ui.screens.album.AlbumScreen
import com.musicapp.ui.screens.artist.ArtistScreen
import com.musicapp.ui.screens.home.HomeScreen
import com.musicapp.ui.screens.library.LibraryScreen
import com.musicapp.ui.screens.main.MainScreen
import com.musicapp.ui.screens.playlist.PlaylistScreen
import com.musicapp.ui.screens.playlist.UserPlaylistScreen
import com.musicapp.ui.screens.passwordrecovery.PasswordRecoveryScreen
import com.musicapp.ui.screens.profile.ProfileScreen
import com.musicapp.ui.screens.search.SearchScreen
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
    @Serializable data class Album(val id: Long): MusicAppRoute
    @Serializable data class Playlist(val id: Long): MusicAppRoute
    @Serializable data class Artist(val id: Long): MusicAppRoute
    @Serializable data class UserPlaylist(val uuid: String): MusicAppRoute
    @Serializable data object Home: MusicAppRoute
    @Serializable data object Search: MusicAppRoute
    @Serializable data object Library: MusicAppRoute
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

@Composable
fun HomeNavGraph(mainNavController: NavController, subNavController: NavHostController) {
    NavHost(
        navController = subNavController,
        startDestination = MusicAppRoute.Home
    ) {
        composable<MusicAppRoute.Album> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.Album>()
            AlbumScreen(subNavController, route.id)
        }
        composable<MusicAppRoute.Playlist> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.Playlist>()
            PlaylistScreen(subNavController, route.id)
        }
        composable<MusicAppRoute.Artist> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.Artist>()
            ArtistScreen(subNavController, route.id)
        }
        composable<MusicAppRoute.UserPlaylist> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.UserPlaylist>()
            UserPlaylistScreen(subNavController, route.uuid)
        }
        composable<MusicAppRoute.Home> {
            HomeScreen(mainNavController, subNavController)
        }
    }
}

@Composable
fun SearchNavGraph(mainNavController: NavController, subNavController: NavHostController) {
    NavHost(
        navController = subNavController,
        startDestination = MusicAppRoute.Search
    ) {
        composable<MusicAppRoute.Album> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.Album>()
            AlbumScreen(subNavController, route.id)
        }
        composable<MusicAppRoute.Playlist> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.Playlist>()
            PlaylistScreen(subNavController, route.id)
        }
        composable<MusicAppRoute.Artist> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.Artist>()
            ArtistScreen(subNavController, route.id)
        }
        composable<MusicAppRoute.UserPlaylist> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.UserPlaylist>()
            UserPlaylistScreen(subNavController, route.uuid)
        }
        composable<MusicAppRoute.Search> {
            SearchScreen(mainNavController, subNavController)
        }
    }
}

@Composable
fun LibraryNavGraph(mainNavController: NavController, subNavController: NavHostController) {
    NavHost(
        navController = subNavController,
        startDestination = MusicAppRoute.Library
    ) {
        composable<MusicAppRoute.Album> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.Album>()
            AlbumScreen(subNavController, route.id)
        }
        composable<MusicAppRoute.Playlist> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.Playlist>()
            PlaylistScreen(subNavController, route.id)
        }
        composable<MusicAppRoute.Artist> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.Artist>()
            ArtistScreen(subNavController, route.id)
        }
        composable<MusicAppRoute.UserPlaylist> { backStackEntry ->
            val route = backStackEntry.toRoute<MusicAppRoute.UserPlaylist>()
            UserPlaylistScreen(subNavController, route.uuid)
        }
        composable<MusicAppRoute.Library> {
            LibraryScreen(mainNavController, subNavController)
        }
    }
}