package com.example.track_me_mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.track_me_mobile.features.teams.presentation.TeamCreateScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.*
import com.example.track_me_mobile.features.teams.presentation.* // Импортирует все ваши экраны
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
@Preview
@Composable
fun App() {
    // Состояние навигации
    var currentScreen by remember { mutableStateOf("create") }
    var teamData by remember { mutableStateOf(TeamFilterData()) }

    MaterialTheme {
        // Добавляем Surface, чтобы фон и перерисовка работали корректно
        androidx.compose.material3.Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                "create" -> TeamCreateScreen(
                    onBackClick = { /* логика */ },
                    onNavigateToInfo = { newData ->
                        // Важно: создаем копию данных, чтобы стейт точно обновился
                        teamData = newData.copy()
                        currentScreen = "info"
                    }
                )
                "info" -> TeamInfoScreen(
                    data = teamData,
                    onBackClick = { currentScreen = "create" },
                    onEditClick = { currentScreen = "edit" }
                )
                "edit" -> TeamEditScreen(
                    initialData = teamData,
                    onBackClick = { currentScreen = "info" },
                    onSaveClick = { updated ->
                        teamData = updated.copy()
                        currentScreen = "info"
                    },
                    onDeactivateClick = { currentScreen = "create" }
                )
            }
        }
    }
}
