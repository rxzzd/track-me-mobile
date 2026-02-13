package com.example.track_me_mobile.core.di

import com.example.track_me_mobile.core.network.HttpClientFactory
import com.example.track_me_mobile.features.auth.data.AuthRepositoryImpl
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.presentation.LoginViewModel
import org.koin.dsl.module

val appModule = module {
    single { HttpClientFactory.create() } // 1. Клиент
    single<AuthRepository> { AuthRepositoryImpl(get()) } // 2. Репозиторий (берет клиент через get())
    factory { LoginViewModel(get()) } // 3. ViewModel (берет репозиторий через get())
}