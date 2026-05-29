package com.example.track_me_mobile.core.di

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.network.HttpClientFactory
import com.example.track_me_mobile.core.network.SessionStorage
import com.example.track_me_mobile.core.ui.components.GlobalHeaderViewModel
import com.example.track_me_mobile.features.auth.data.AuthRepositoryImpl
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.domain.AuthSessionSaver
import com.example.track_me_mobile.features.auth.presentation.LoginViewModel
import com.example.track_me_mobile.features.profile.data.ProfileRepositoryImpl
import com.example.track_me_mobile.features.profile.domain.ProfileRepository
import com.example.track_me_mobile.features.profile.presentation.ProfileViewModel
import com.example.track_me_mobile.features.profile.presentation.UserProfileViewModel
import com.example.track_me_mobile.features.streams.data.StreamRepositoryImpl
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.presentation.AddStreamViewModel
import com.example.track_me_mobile.features.streams.presentation.EditStreamViewModel
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


    single { PersistentStorage.create() }
    single { SessionStorage(get<com.example.track_me_mobile.core.storage.PersistentStorage>()) }

    single {
        HttpClientFactory.create(
            sessionStorage = get<com.example.track_me_mobile.core.network.SessionStorage>(),
            onUnauthorized = {
                println("[App] Unauthorized - session cleared")
            }
        )
    }
    single { UserInfoHolder() }


    factory { SplashViewModel(get(), get(), get()) }


    single { AuthRepositoryImpl(get(), get()) }
    single<AuthRepository> { get<AuthRepositoryImpl>() }
    single<AuthSessionSaver> { get<AuthRepositoryImpl>() }
    factory { LoginViewModel(get(), get(), get()) }
    factory { GlobalHeaderViewModel(get(), get()) }


    single<StreamRepository> { StreamRepositoryImpl(get()) }
    factory { StreamListViewModel(get()) }
    factory { AddStreamViewModel(get()) }
    factory { (streamId: String) -> EditStreamViewModel(get(), streamId) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    factory { ProfileViewModel(get()) }


    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    factory { ProfileViewModel(get()) }
    factory { (username: String) -> UserProfileViewModel(get(), username) }

    single<UsersRepository> { UsersRepositoryImpl(get()) }
    factory { TrackerListViewModel(get()) }
    factory { AdminListViewModel(get()) }


    single<TeamRepository> { TeamRepositoryImpl(get(), get()) }
    factory { TeamListViewModel(get()) }


    single<TeamCardRepository> { TeamCardRepositoryImpl(get(), get()) }
    factory { (teamId: String) -> TeamCardViewModel(teamId, get()) }
    factory { TeamCreateViewModel(get(), get()) }
    factory { (teamId: String) -> TeamEditViewModel(teamId, get(), get()) }


    single<com.example.track_me_mobile.core.feedback.domain.FeedbackRepository> {
        com.example.track_me_mobile.core.feedback.data.FeedbackRepositoryImpl(get())
    }
    factory {
        com.example.track_me_mobile.core.feedback.presentation.FeedbackViewModel(get(), get())
    }

    single<com.example.track_me_mobile.features.reports.domain.ReportRepository> {
        com.example.track_me_mobile.features.reports.data.ReportRepositoryImpl(get())
    }
    single<com.example.track_me_mobile.features.reports.domain.StreamMeetingReportRepository> {
        com.example.track_me_mobile.features.reports.data.StreamMeetingReportRepositoryImpl(get())
    }
    factory {
        com.example.track_me_mobile.features.reports.presentation.ReportsViewModel(
            repository = get(),
            userInfoHolder = get(),
            httpClient = get()
        )
    }
    factory { (streamId: String) ->
        com.example.track_me_mobile.features.reports.presentation.StreamMeetingReportViewModel(
            get(),
            streamId
        )
    }

    single<MeetingRepository> { MeetingRepositoryImpl(get()) }


    factory { (teamId: String) ->
        TeamMeetingsViewModel(teamId, get())
    }


    factory { (meetingId: String, teamCardId: String) ->
        MeetingViewModel(get(), meetingId, teamCardId, get())
    }
}