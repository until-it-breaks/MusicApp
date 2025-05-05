package com.musicapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.musicapp.data.remote.DeezerDataSource
import com.musicapp.ui.screens.login.LoginViewModel
import com.musicapp.ui.screens.passwordrecovery.PasswordRecoveryViewModel
import com.musicapp.ui.screens.profile.ProfileScreenViewModel
import com.musicapp.ui.screens.signup.SignUpViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }

    single { FirebaseFirestore.getInstance() }

    single { HttpClient() }

    single { DeezerDataSource(get()) }

    viewModel { SignUpViewModel(get(), get()) }

    viewModel { LoginViewModel(get()) }

    viewModel { PasswordRecoveryViewModel(get()) }

    viewModel { ProfileScreenViewModel(get()) }

    viewModel { MainViewModel(get()) }
}