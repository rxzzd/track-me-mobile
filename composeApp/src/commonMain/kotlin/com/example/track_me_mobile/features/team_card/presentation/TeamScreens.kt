package com.example.track_me_mobile.features.team_card.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf

// ─── Просмотр карточки (данные с бэка) ───────────────────────────────────────
data class InfoTeamLevel(val teamId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = remember(teamId) {
            object : KoinComponent {}.getKoin().get<TeamCardViewModel> { parametersOf(teamId) }
        }
        TeamInfoScreen(
            viewModel       = viewModel,
            onBackClick     = { navigator.pop() },
            onEditClick     = { /* TODO */ },
            onMeetingsClick = { /* TODO */ }
        )
    }
}

// ─── Создание команды ─────────────────────────────────────────────────────────
class CreateTeamLevel : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<TeamCreateViewModel>()
        TeamCreateScreen(
            viewModel   = viewModel,
            onBackClick = { navigator.pop() },
            onCreated   = { navigator.pop() }   // после создания — назад к списку
        )
    }
}

// ─── Редактирование команды ───────────────────────────────────────────────────
class EditTeamLevel(private val viewModel: TeamViewModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeamEditScreen(
            viewModel         = viewModel,
            onBackClick       = { navigator.pop() },
            onSaveClick       = { navigator.pop() },
            onDeactivateClick = { navigator.popUntilRoot() },
            onFilterClick     = { navigator.push(FilterTeamLevel(viewModel)) }
        )
    }
}

// ─── Встречи ──────────────────────────────────────────────────────────────────
data class MeetingsTeamLevel(val meetings: List<MeetingData>) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeamMeetingsScreen(
            meetings           = meetings,
            onBackClick        = { navigator.pop() },
            onPlanMeetingClick = { }
        )
    }
}

// ─── Фильтр ───────────────────────────────────────────────────────────────────
class FilterTeamLevel(private val viewModel: TeamViewModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeamFilterScreen(
            viewModel = viewModel,
            onClose   = { navigator.pop() }
        )
    }
}