package com.example.track_me_mobile.core.di

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.network.HttpClientFactory
import com.example.track_me_mobile.core.network.SessionStorage
import com.example.track_me_mobile.core.ui.components.GlobalHeaderViewModel
import com.example.track_me_mobile.features.auth.data.AuthRepositoryImpl
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.presentation.LoginViewModel
import com.example.track_me_mobile.features.profile.data.ProfileRepositoryImpl
import com.example.track_me_mobile.features.profile.domain.ProfileRepository
import com.example.track_me_mobile.features.profile.presentation.ProfileViewModel
import com.example.track_me_mobile.features.profile.presentation.UserProfileViewModel
import com.example.track_me_mobile.features.streams.data.StreamRepositoryImpl
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.presentation.AddStreamViewModel
import com.example.track_me_mobile.features.streams.presentation.StreamListViewModel
import com.example.track_me_mobile.features.team_card.data.TeamCardRepositoryImpl
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
import com.example.track_me_mobile.features.team_card.presentation.TeamCardViewModel
import com.example.track_me_mobile.features.team_card.presentation.TeamCreateViewModel
import com.example.track_me_mobile.features.team_card.presentation.TeamEditViewModel
import com.example.track_me_mobile.features.team_card.presentation.TeamMeetingsViewModel
import com.example.track_me_mobile.features.teams.data.TeamRepositoryImpl
import com.example.track_me_mobile.features.teams.domain.TeamRepository
import com.example.track_me_mobile.features.teams.presentation.TeamListViewModel
import com.example.track_me_mobile.features.users.data.UsersRepositoryImpl
import com.example.track_me_mobile.features.users.domain.UsersRepository
import com.example.track_me_mobile.features.users.presentation.AdminListViewModel
import com.example.track_me_mobile.features.users.presentation.TrackerListViewModel
import com.example.track_me_mobile.features.meetings.data.MeetingRepositoryImpl
import com.example.track_me_mobile.features.meetings.domain.MeetingRepository
import com.example.track_me_mobile.features.meetings.presentation.MeetingViewModel
import com.example.track_me_mobile.features.splash.SplashViewModel
import org.koin.dsl.module
import com.example.track_me_mobile.core.storage.PersistentStorage

val appModule = module {

    // ── Core ────────────────────────────────────────────────────────────────
    single { PersistentStorage.create() } // ← ДОБАВИТЬ
    single { SessionStorage(get()) } // ← ИЗМЕНИТЬ (теперь принимает PersistentStorage)

    single {
        HttpClientFactory.create(
            sessionStorage = get(),
            onUnauthorized = {
                println("[App] Unauthorized - session cleared")
            }
        )
    }
    single { UserInfoHolder() }

    // ── Splash ──────────────────────────────────────────────────────────────
    factory { SplashViewModel(get(), get(), get()) }

    // ── Auth ────────────────────────────────────────────────────────────────
    single { AuthRepositoryImpl(get(), get()) }
    single<AuthRepository> { get<AuthRepositoryImpl>() }
    factory { LoginViewModel(get(), get(), get()) }
    factory { GlobalHeaderViewModel(get(), get()) }

    // ── Streams ─────────────────────────────────────────────────────────────
    single<StreamRepository> { StreamRepositoryImpl(get()) }
    factory { StreamListViewModel(get()) }
    factory { AddStreamViewModel(get()) }

    // ── Profile ─────────────────────────────────────────────────────────────
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    factory { ProfileViewModel(get()) }
    factory { (username: String) -> UserProfileViewModel(get(), username) }

    // ── Users ────────────────────────────────────────────────────────────────
    single<UsersRepository> { UsersRepositoryImpl(get()) }
    factory { TrackerListViewModel(get()) }
    factory { AdminListViewModel(get()) }

    // ── Teams ────────────────────────────────────────────────────────────────
    single<TeamRepository> { TeamRepositoryImpl(get(), get()) }
    factory { TeamListViewModel(get()) }

    // ── Team Card ────────────────────────────────────────────────────────────
    single<TeamCardRepository> { TeamCardRepositoryImpl(get(), get()) }
    factory { (teamId: String) -> TeamCardViewModel(teamId, get()) }
    factory { TeamCreateViewModel(get(), get()) }
    factory { (teamId: String) -> TeamEditViewModel(teamId, get(), get()) }

    // ── Meetings ─────────────────────────────────────────────────────────────
    single<MeetingRepository> { MeetingRepositoryImpl(get()) }

    // TeamMeetingsViewModel - для списка встреч команды (с teamId)
    factory { (teamId: String) ->
        TeamMeetingsViewModel(teamId, get())
    }

    // MeetingViewModel - для конкретной встречи (с meetingId)
    factory { (meetingId: String, teamCardId: String) ->  // ← ДВА ПАРАМЕТРА
        MeetingViewModel(get(), meetingId, teamCardId)
    }
}