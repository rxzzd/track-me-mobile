package com.example.track_me_mobile.features.team_card.presentation

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.lifecycle.viewmodel.compose.viewModel

class CreateTeamLevel : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: TeamViewModel = viewModel()

        TeamCreateScreen(
            viewModel = viewModel,
            onBackClick = { navigator.pop() },
            onNavigateToInfo = { navigator.push(InfoTeamLevel(viewModel)) }
        )
    }
}

class InfoTeamLevel(private val viewModel: TeamViewModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        TeamInfoScreen(
            viewModel = viewModel,
            onBackClick = { navigator.pop() },
            onEditClick = { navigator.push(EditTeamLevel(viewModel)) },
            onMeetingsClick = { navigator.push(MeetingsTeamLevel(listOf())) },
            onFilterClick = { navigator.push(FilterTeamLevel(viewModel)) }  // ← добавить
        )
    }
}

class EditTeamLevel(private val viewModel: TeamViewModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        TeamEditScreen(
            viewModel = viewModel,
            onBackClick = { navigator.pop() },
            onSaveClick = { navigator.pop() },
            onDeactivateClick = { navigator.popUntilRoot() },
            onFilterClick = { navigator.push(FilterTeamLevel(viewModel)) }  // ← добавить
        )
    }
}

data class MeetingsTeamLevel(val meetings: List<MeetingData>) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        TeamMeetingsScreen(
            meetings = meetings,
            onBackClick = { navigator.pop() },
            onPlanMeetingClick = { }
        )
    }
}
class FilterTeamLevel(private val viewModel: TeamViewModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        TeamFilterScreen(
            viewModel = viewModel,
            onClose = { navigator.pop() }
        )
    }
}