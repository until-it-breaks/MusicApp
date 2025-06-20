package com.musicapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.musicapp.data.database.MusicAppDatabase
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.data.repositories.SettingsRepository
import com.musicapp.data.repositories.TrackHistoryRepository
import com.musicapp.data.repositories.TracksRepository
import com.musicapp.data.repositories.UserPlaylistRepository
import com.musicapp.data.repositories.UserRepository
import com.musicapp.playback.BasePlaybackViewModel
import com.musicapp.playback.MediaPlayerManager
import com.musicapp.auth.AuthManager
import com.musicapp.ui.screens.addtoplaylist.AddToPlaylistViewModel
import com.musicapp.ui.screens.album.AlbumViewModel
import com.musicapp.ui.screens.artist.ArtistViewModel
import com.musicapp.ui.screens.home.HomeViewModel
import com.musicapp.ui.screens.library.LibraryViewModel
import com.musicapp.ui.screens.login.LoginViewModel
import com.musicapp.ui.screens.password.PasswordRecoveryViewModel
import com.musicapp.ui.screens.playlist.LikedTracksViewModel
import com.musicapp.ui.screens.playlist.PersonalPlaylistViewModel
import com.musicapp.ui.screens.playlist.PublicPlaylistViewModel
import com.musicapp.ui.screens.playlist.TrackHistoryViewModel
import com.musicapp.ui.screens.profile.ProfileScreenViewModel
import com.musicapp.ui.screens.search.SearchViewModel
import com.musicapp.ui.screens.settings.SettingsViewModel
import com.musicapp.ui.screens.signup.SignUpViewModel
import com.musicapp.ui.screens.trackdetails.TrackDetailsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("settings")

@SuppressLint("UnsafeOptInUsageError")
val appModule = module {

    // Firebase Auth
    single { FirebaseAuth.getInstance() }

    // Preferences
    single { get<Context>().dataStore }

    single { SettingsRepository(get()) }

    // Room database
    single {
        Room.databaseBuilder(
            get(),
            MusicAppDatabase::class.java,
            "music-app"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    // Room database repositories
    single {
        UserPlaylistRepository(
            get(),
            get<MusicAppDatabase>().playlistDAO(),
            get(),
            get<Context>()
        )
    }

    single {
        LikedTracksRepository(
            get(),
            get<MusicAppDatabase>().likedPlaylistDAO(),
            get()
        )
    }

    single {
        TrackHistoryRepository(
            get(),
            get(),
            get<MusicAppDatabase>().trackHistoryDAO()
        )
    }

    single {
        TracksRepository(
            get(),
            get<MusicAppDatabase>().trackDAO()
        )
    }

    single {
        UserRepository(
            get<MusicAppDatabase>().userDAO(),
            get<MusicAppDatabase>().likedPlaylistDAO(),
            get<MusicAppDatabase>().trackHistoryDAO(),
            get()
        )
    }

    // HTTP clients
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = false
                    }
                )
            }
        }
    }

    // Deezer API data source
    single { DeezerDataSource(get()) }

    // Media player
    single { MediaPlayerManager(get(), get(), get()) }

    // Authentication helper
    single { AuthManager(get())}

    // ViewModels
    viewModel { SignUpViewModel(get(), get()) }

    viewModel { LoginViewModel(get(), get()) }

    viewModel { PasswordRecoveryViewModel(get()) }

    viewModel { ProfileScreenViewModel(get(), get(), androidContext()) }

    viewModel { MainActivityViewModel(get(), get()) }

    viewModel { HomeViewModel(get(), get()) }

    viewModel { AlbumViewModel(get(), get(), get(), get()) }

    viewModel { PublicPlaylistViewModel(get(), get(), get(), get(), get()) }

    viewModel { ArtistViewModel(get(), get()) }

    viewModel { LibraryViewModel(get(), get()) }

    viewModel { PersonalPlaylistViewModel(get(), get()) }

    viewModel { LikedTracksViewModel(get(), get(), get()) }

    viewModel { SearchViewModel(get(), get(), get(), get(), get()) }
    
    viewModel { TrackHistoryViewModel(get(), get(), get()) }

    viewModel { AddToPlaylistViewModel(get(), get(), get()) }

    viewModel { SettingsViewModel(get(), get(), get()) }

    viewModel { TrackDetailsViewModel(get(), get(), get()) }

    viewModel { BasePlaybackViewModel(get()) }
}