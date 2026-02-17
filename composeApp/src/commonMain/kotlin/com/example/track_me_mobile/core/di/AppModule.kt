package com.example.track_me_mobile.core.di

import com.example.track_me_mobile.core.network.HttpClientFactory
import com.example.track_me_mobile.core.network.SessionStorage
import com.example.track_me_mobile.features.auth.data.AuthRepositoryImpl
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.presentation.LoginViewModel
import org.koin.dsl.module

val appModule = module {
    single { SessionStorage() }
    single { HttpClientFactory.create(get()) }

    // Регистрируем AuthRepositoryImpl и как конкретный класс, и как интерфейс.
    // LoginViewModel требует оба: интерфейс для getUserInfo, impl для saveSession.
    single { AuthRepositoryImpl(get(), get()) }
    single<AuthRepository> { get<AuthRepositoryImpl>() }

    factory { LoginViewModel(get(), get()) }
}