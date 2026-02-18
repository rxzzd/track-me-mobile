package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class CreateTeamLevel : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        TeamCreateScreen(
            onBackClick = { navigator.pop() },
            onNavigateToInfo = { teamData ->
                // Переходим на экран информации
                navigator.push(InfoTeamLevel(teamData))
            }
        )
    }
}

data class InfoTeamLevel(val initialData: TeamFilterData) : Screen {
    override val key: String = "InfoTeamScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var currentData by remember { mutableStateOf(initialData) }

        // Следим за верхним экраном — когда EditTeamLevel закрылся, читаем результат
        val topScreen = navigator.lastItem
        LaunchedEffect(topScreen) {
            if (topScreen is InfoTeamLevel) {
                // мы снова наверху — данные уже в currentData
            }
        }

        TeamInfoScreen(
            data = currentData,
            onBackClick = { navigator.pop() },
            onEditClick = {
                navigator.push(EditTeamLevel(currentData) { updated ->
                    currentData = updated  // обновляем до push
                })
            },
            onMeetingsClick = { navigator.push(MeetingsTeamLevel(listOf())) }
        )
    }
}

data class EditTeamLevel(
    val initialData: TeamFilterData,
    val onSave: (TeamFilterData) -> Unit
) : Screen {

    @Composable
    override fun Content() { // Проверьте наличие override здесь!
        val navigator = LocalNavigator.currentOrThrow
        TeamEditScreen(
            initialData = initialData,
            onBackClick = { navigator.pop() },
            onSaveClick = { updatedData ->
                onSave(updatedData)
                navigator.pop()
            },
            onDeactivateClick = { navigator.popUntilRoot() }
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
                onPlanMeetingClick = { /* Логика */ }
            )
        }
    }

