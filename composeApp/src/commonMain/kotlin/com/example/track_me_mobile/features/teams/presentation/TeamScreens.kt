package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

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
    override fun Content() { // Убедитесь, что здесь есть override
        val navigator = LocalNavigator.currentOrThrow

        // Используем remember(initialData), чтобы стейт обновлялся при смене данных
        var currentData by remember(initialData) { mutableStateOf(initialData) }

        TeamInfoScreen(
            data = currentData,
            onBackClick = { navigator.pop() },
            onEditClick = {
                navigator.push(EditTeamLevel(
                    initialData = currentData,
                    onSave = { updated -> currentData = updated }
                ))
            },
            onMeetingsClick = {
                              navigator.push(MeetingsTeamLevel(listOf())) // Заглушка встреч
                }
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

