package com.example.track_me_mobile.core.di

import com.example.track_me_mobile.core.domain.UserInfoHolder
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
import com.example.track_me_mobile.features.teams.data.TeamRepositoryImpl
import com.example.track_me_mobile.features.teams.domain.TeamRepository
import com.example.track_me_mobile.features.teams.presentation.TeamListViewModel
import com.example.track_me_mobile.features.auth.presentation.RegistrationViewModel
import com.example.track_me_mobile.features.auth.domain.RegistrationRepository
import com.example.track_me_mobile.features.auth.data.RegistrationRepositoryImpl
import org.koin.dsl.module
import com.example.track_me_mobile.features.profile.presentation.UserProfileViewModel
import com.example.track_me_mobile.features.streams.data.StreamRepositoryImpl
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.presentation.AddStreamViewModel
import com.example.track_me_mobile.features.streams.presentation.EditStreamViewModel
import com.example.track_me_mobile.features.streams.presentation.StreamListViewModel
import com.example.track_me_mobile.features.users.presentation.TrackerListViewModel
import com.example.track_me_mobile.features.users.data.UsersRepositoryImpl
import com.example.track_me_mobile.features.users.domain.UsersRepository
import org.koin.dsl.module

val appModule = module {
    // Core
    single { SessionStorage() }
    single { HttpClientFactory.create(get()) }
    single { UserInfoHolder() }

    // Auth
    single { AuthRepositoryImpl(get(), get()) }
    single<AuthRepository> { get<AuthRepositoryImpl>() }
    factory { LoginViewModel(get(), get(), get()) }  // AuthRepository, AuthRepositoryImpl, UserInfoHolder
    factory { GlobalHeaderViewModel(get(), get()) }  // AuthRepository, SessionStorage

    

    // Streams
    single<StreamRepository> { StreamRepositoryImpl(get()) }
    factory { StreamListViewModel(get()) }
    factory { AddStreamViewModel(get()) }
    factory { (streamId: String) -> EditStreamViewModel(get(), streamId) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    factory { ProfileViewModel(get()) }

    // Profile
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    factory { ProfileViewModel(get()) }
    factory { (username: String) -> UserProfileViewModel(get(), username) }

    // Users (shared repository for trackers and admins)
    single<UsersRepository> { UsersRepositoryImpl(get()) }
    factory { TrackerListViewModel(get()) }
    factory { AdminListViewModel(get()) }

    // Teams
    single<TeamRepository> { TeamRepositoryImpl(get(), get()) }  // HttpClient, UserInfoHolder
    factory { TeamListViewModel(get()) }
}