package com.example.track_me_mobile.core.di

import com.example.track_me_mobile.core.network.HttpClientFactory
import com.example.track_me_mobile.core.network.SessionStorage
import com.example.track_me_mobile.core.ui.components.GlobalHeaderViewModel
import com.example.track_me_mobile.features.auth.data.AuthRepositoryImpl
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.presentation.LoginViewModel
import com.example.track_me_mobile.features.profile.data.ProfileRepositoryImpl
import com.example.track_me_mobile.features.profile.domain.ProfileRepository
import com.example.track_me_mobile.features.profile.presentation.ProfileViewModel
import org.koin.dsl.module
import com.example.track_me_mobile.features.streams.data.StreamRepositoryImpl
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.presentation.StreamListViewModel
val appModule = module {
    single { SessionStorage() }
    single { HttpClientFactory.create(get()) }

    // Регистрируем AuthRepositoryImpl и как конкретный класс, и как интерфейс.
    // LoginViewModel требует оба: интерфейс для getUserInfo, impl для saveSession.
    single { AuthRepositoryImpl(get(), get()) }
    single<AuthRepository> { get<AuthRepositoryImpl>() }
    single<StreamRepository> { StreamRepositoryImpl(get()) }
    factory { StreamListViewModel(get()) }
    factory { LoginViewModel(get(), get()) }
    factory { GlobalHeaderViewModel(get(), get()) }

    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    factory { ProfileViewModel(get()) }
}