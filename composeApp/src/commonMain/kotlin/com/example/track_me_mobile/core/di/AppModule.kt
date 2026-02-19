package com.example.track_me_mobile.core.di

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.network.HttpClientFactory
import com.example.track_me_mobile.core.network.SessionStorage
import com.example.track_me_mobile.features.auth.data.AuthRepositoryImpl
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.presentation.LoginViewModel
import com.example.track_me_mobile.features.teams.data.TeamRepositoryImpl
import com.example.track_me_mobile.features.teams.domain.TeamRepository
import com.example.track_me_mobile.features.teams.presentation.TeamListViewModel
import com.example.track_me_mobile.features.auth.presentation.RegistrationViewModel
import com.example.track_me_mobile.features.auth.domain.RegistrationRepository
import com.example.track_me_mobile.features.auth.data.RegistrationRepositoryImpl


import org.koin.dsl.module

val appModule = module {
    // Core
    single { SessionStorage() }
    single { HttpClientFactory.create(get()) }
    single { UserInfoHolder() }          // ← хранит UserInfo между экранами

    // Auth
    single { AuthRepositoryImpl(get(), get()) }
    single<AuthRepository> { get<AuthRepositoryImpl>() }
    factory { LoginViewModel(get(), get(), get()) }   // ← +UserInfoHolder

    // Registration — отдельный репозиторий, публичные запросы без сессии
    single<RegistrationRepository> { RegistrationRepositoryImpl(get()) }
    factory { RegistrationViewModel(get()) }

    // Teams
    single<TeamRepository> { TeamRepositoryImpl(get(), get()) }  // client + userInfoHolder
    factory { TeamListViewModel(get()) }
}