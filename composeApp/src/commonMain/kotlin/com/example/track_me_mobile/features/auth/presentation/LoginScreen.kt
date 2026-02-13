package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.components.TrackMeButton
import com.example.track_me_mobile.core.ui.components.TrackMePasswordField
import com.example.track_me_mobile.core.ui.components.TrackMeTextField
import com.example.track_me_mobile.core.ui.theme.TrackMeDeepPurple
import com.example.track_me_mobile.core.ui.theme.TrackMeTextSecondary

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // Используем Koin для получения ViewModel
        val viewModel = koinScreenModel<LoginViewModel>()

        LoginScreenContent(
            username = viewModel.username,
            password = viewModel.password,
            isLoading = viewModel.isLoading,
            errorMessage = viewModel.errorMessage,
            onUsernameChange = viewModel::onUsernameChanged,
            onPasswordChange = viewModel::onPasswordChanged,
            onLoginClick = {
                viewModel.onLoginClick {
                    // Действие при успешном входе
                    // navigator.replace(MainScreen()) // Например
                }
            },
            onRegisterClick = {
                // navigator.push(RegisterScreen())
            }
        )
    }
}

@Composable
fun LoginScreenContent(
    username: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 40.dp), // Большие отступы как на макете
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Заголовок
            Text(
                text = "Вход",
                style = MaterialTheme.typography.displayMedium,
                color = TrackMeDeepPurple,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Поля ввода
            TrackMeTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = "Имя пользователя telegram"
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrackMePasswordField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Пароль"
            )

            // Ссылка восстановления
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Забыли пароль? Восстановить?",
                style = MaterialTheme.typography.bodySmall,
                color = TrackMeTextSecondary,
                modifier = Modifier.clickable { /* Навигация */ }
            )

            // Отображение ошибки
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Состояние кнопки или загрузки
            if (isLoading) {
                CircularProgressIndicator(color = TrackMeDeepPurple)
            } else {
                TrackMeButton(
                    text = "Войти",
                    onClick = onLoginClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ссылка на регистрацию
            Text(
                text = "Зарегистрироваться",
                style = MaterialTheme.typography.bodyMedium,
                color = TrackMeTextSecondary,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }
    }
}

// Превью больше не будет падать, так как мы вынесли логику навигатора
@androidx.compose.ui.tooling.preview.Preview
@Composable
fun LoginPreview() {
    LoginScreenContent(
        username = "ivanov_ivan",
        password = "123",
        isLoading = false,
        errorMessage = null,
        onUsernameChange = {},
        onPasswordChange = {},
        onLoginClick = {},
        onRegisterClick = {}
    )
}