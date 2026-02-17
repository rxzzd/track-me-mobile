package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.runtime.rememberCoroutineScope
class CreateTeamLevel : Screen {
    @Composable
    override fun Content() { // Проверьте, что здесь есть фигурная скобка
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        TeamCreateScreen(
            onBackClick = { navigator.pop() },
            onNavigateToInfo = { teamData ->
                navigator.push(InfoTeamLevel(teamData))
            }
        )
    }
}

data class InfoTeamLevel(val data: TeamFilterData) : Screen {
    // Генерируем уникальный ключ на основе хэша данных
    override val key: String = "InfoTeamScreen_${data.hashCode()}"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeamInfoScreen(
            data = data,
            onBackClick = { navigator.pop() },
            onEditClick = { navigator.push(EditTeamLevel(data)) }
        )
    }
}

data class EditTeamLevel(val initialData: TeamFilterData) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // ВЫЗОВ ФУНКЦИИ ИЗ TeamEditScreen.kt
        TeamEditScreen(
            initialData = initialData,
            onBackClick = { navigator.pop() },
            onSaveClick = { updatedData ->
                navigator.pop()
            },
            onDeactivateClick = { navigator.popUntilRoot() }
        )
    }
}