package com.musicapp

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.musicapp.data.database.MusicAppDatabase
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.repositories.PlaylistsRepository
import com.musicapp.data.repositories.TracksRepository
import com.musicapp.data.repositories.UsersRepository
import com.musicapp.ui.screens.login.LoginViewModel
import com.musicapp.ui.screens.main.AlbumViewModel
import com.musicapp.ui.screens.main.ArtistViewModel
import com.musicapp.ui.screens.main.PlaylistViewModel
import com.musicapp.ui.screens.main.home.HomeViewModel
import com.musicapp.ui.screens.passwordrecovery.PasswordRecoveryViewModel
import com.musicapp.ui.screens.profile.ProfileScreenViewModel
import com.musicapp.ui.screens.signup.SignUpViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }

    single { FirebaseFirestore.getInstance() }

    single {
        Room.databaseBuilder(
            get(),
            MusicAppDatabase::class.java,
            "music-app"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        PlaylistsRepository(get<MusicAppDatabase>().playlistDAO(), get())
    }

    single {
        TracksRepository(get<MusicAppDatabase>().trackDAO(), get())
    }

    single {
        UsersRepository(get<MusicAppDatabase>().userDAO(), get())
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

    viewModel { SignUpViewModel(get(), get()) }

    viewModel { LoginViewModel(get()) }

    viewModel { PasswordRecoveryViewModel(get()) }

    viewModel { ProfileScreenViewModel(get()) }

    viewModel { MainViewModel(get()) }

    viewModel { HomeViewModel() }

    viewModel { AlbumViewModel() }

    viewModel { PlaylistViewModel() }

    viewModel { ArtistViewModel() }
}