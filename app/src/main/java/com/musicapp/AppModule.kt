package com.musicapp

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.musicapp.data.database.MusicAppDatabase
import com.musicapp.data.remote.deezer.DeezerDataSource
import com.musicapp.data.repositories.LikedTracksRepository
import com.musicapp.data.repositories.TrackHistoryRepository
import com.musicapp.data.repositories.TracksRepository
import com.musicapp.data.repositories.UserPlaylistRepository
import com.musicapp.data.repositories.UsersRepository
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
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single {
        UserPlaylistRepository(
            get<MusicAppDatabase>().playlistDAO(),
            get()
        )
    }

    single {
        LikedTracksRepository(
            get(),
            get<MusicAppDatabase>().likedTracksDAO(),
            get()
        )
    }

    single {
        TrackHistoryRepository(
            get(),
            get<MusicAppDatabase>().trackHistoryDAO()
        )
    }

    single {
        TracksRepository(get<MusicAppDatabase>().trackDAO())
    }

    single {
        UsersRepository(get<MusicAppDatabase>().userDAO(),)
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

    viewModel { SignUpViewModel(get(), get(), get(), get(), get()) }

    viewModel { LoginViewModel(get()) }

    viewModel { PasswordRecoveryViewModel(get()) }

    viewModel { ProfileScreenViewModel(get()) }

    viewModel { MainViewModel(get()) }

    viewModel { HomeViewModel(get()) }

    viewModel { AlbumViewModel(get(), get(), get()) }

    viewModel { PublicPlaylistViewModel(get(), get(), get()) }

    viewModel { ArtistViewModel(get()) }

    viewModel { LibraryViewModel(get(), get(), get(), get()) }

    viewModel { PersonalPlaylistViewModel(get()) }

    viewModel { LikedTracksViewModel(get(), get()) }

    viewModel { TrackHistoryViewModel(get(), get()) }

    viewModel { AddToPlaylistViewModel(get(), get(), get()) }
}