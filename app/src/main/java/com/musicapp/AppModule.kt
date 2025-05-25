package com.musicapp

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
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("settings")

val appModule = module {
    single { FirebaseAuth.getInstance() }

    single {
        Room.databaseBuilder(
            get(),
            MusicAppDatabase::class.java,
            "music-app"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    single {
        UserPlaylistRepository(
            get(),
            get<MusicAppDatabase>().playlistDAO(),
            get()
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
            get<TracksRepository>(),
            get<MusicAppDatabase>().trackHistoryDAO()
        )
    }

    single {
        TracksRepository(get<MusicAppDatabase>().trackDAO())
    }

    single {
        UserRepository(
            get<MusicAppDatabase>().userDAO(),
            get<MusicAppDatabase>().likedPlaylistDAO(),
            get<MusicAppDatabase>().trackHistoryDAO(),
            get()
        )
    }

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

    single { DeezerDataSource(get()) }

    single { get<Context>().dataStore }

    single { SettingsRepository(get()) }

    viewModel { SignUpViewModel(get(), get()) }

    viewModel { LoginViewModel(get()) }

    viewModel { PasswordRecoveryViewModel(get()) }

    viewModel { ProfileScreenViewModel(get()) }

    viewModel { MainActivityViewModel(get(), get()) }

    viewModel { HomeViewModel(get()) }

    viewModel { AlbumViewModel(get(), get(), get()) }

    viewModel { PublicPlaylistViewModel(get(), get(), get()) }

    viewModel { ArtistViewModel(get()) }

    viewModel { LibraryViewModel(get(), get()) }

    viewModel { PersonalPlaylistViewModel(get()) }

    viewModel { LikedTracksViewModel(get(), get()) }

    viewModel { SearchViewModel(get()) }
    
    viewModel { TrackHistoryViewModel(get(), get()) }

    viewModel { AddToPlaylistViewModel(get(), get(), get()) }

    viewModel { SettingsViewModel(get()) }
}