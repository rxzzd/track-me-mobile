package com.example.track_me_mobile.core.di

import com.example.track_me_mobile.core.network.HttpClientFactory
import com.example.track_me_mobile.core.network.SessionStorage
import com.example.track_me_mobile.core.ui.components.GlobalHeaderViewModel
import com.example.track_me_mobile.features.users.presentation.AdminListViewModel
import com.example.track_me_mobile.features.auth.data.AuthRepositoryImpl
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.presentation.LoginViewModel
import com.example.track_me_mobile.features.profile.data.ProfileRepositoryImpl
import com.example.track_me_mobile.features.profile.domain.ProfileRepository
import com.example.track_me_mobile.features.profile.presentation.ProfileViewModel
import com.example.track_me_mobile.features.profile.presentation.UserProfileViewModel
import com.example.track_me_mobile.features.streams.data.StreamRepositoryImpl
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.presentation.StreamListViewModel
import com.example.track_me_mobile.features.users.presentation.TrackerListViewModel
import com.example.track_me_mobile.features.users.data.UsersRepositoryImpl
import com.example.track_me_mobile.features.users.domain.UsersRepository
import org.koin.dsl.module

val appModule = module {
    single { SessionStorage() }
    single { HttpClientFactory.create(get()) }

    // Auth
    single { AuthRepositoryImpl(get(), get()) }
    single<AuthRepository> { get<AuthRepositoryImpl>() }
    factory { LoginViewModel(get(), get()) }

    // Profile
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    factory { ProfileViewModel(get()) }
    factory { (username: String) -> UserProfileViewModel(get(), username) }

    // Users (shared repository for trackers and admins)
    single<UsersRepository> { UsersRepositoryImpl(get()) }
    factory { TrackerListViewModel(get()) }
    factory { AdminListViewModel(get()) }

    // Streams
    single<StreamRepository> { StreamRepositoryImpl(get()) }
    factory { StreamListViewModel(get()) }

    // Global Header
    factory { GlobalHeaderViewModel(get()) }
}