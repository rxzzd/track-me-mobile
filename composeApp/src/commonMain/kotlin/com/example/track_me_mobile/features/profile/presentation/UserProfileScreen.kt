package com.example.track_me_mobile.features.profile.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

data class UserProfileScreen(val username: String) : Screen {
    @Composable
    override fun Content() {
        val viewModel: UserProfileViewModel = koinInject { parametersOf(username) }
        val state = viewModel.state

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TrackMePurple)
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Повторить")
                        }
                    }
                }
            }
            else -> {
                // Используем существующий ProfileScreenContent но без кнопки редактирования
                ProfileScreenContent(
                    name = state.fullName,
                    email = state.email,
                    phone = state.phoneNumber,
                    telegram = "@${state.username}",
                    role = state.roles.firstOrNull() ?: "Пользователь",
                    onNavigateToEdit = { /* Нельзя редактировать чужой профиль */ }
                )
            }
        }
    }
}