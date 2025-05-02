package com.musicapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.musicapp.ui.screens.login.LoginViewModel
import com.musicapp.ui.screens.login.PasswordRecoveryViewModel
import com.musicapp.ui.screens.signup.SignUpViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }

    single { FirebaseFirestore.getInstance() }

    viewModel { SignUpViewModel(get(), get()) }

    viewModel { LoginViewModel(get()) }

    viewModel { PasswordRecoveryViewModel(get()) }
}