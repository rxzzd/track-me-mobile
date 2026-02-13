package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.components.TrackMeButton
import com.example.track_me_mobile.core.ui.components.TrackMePasswordField
import com.example.track_me_mobile.core.ui.components.TrackMeTextField
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.core.ui.theme.TrackMeTextSecondary


class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { LoginViewModel() }

        LoginScreenContent(
            username = viewModel.username,
            password = viewModel.password,
            onUsernameChange = viewModel::onUsernameChanged,
            onPasswordChange = viewModel::onPasswordChanged,
            onLoginClick = { viewModel.onLoginClick() },
            onRegisterClick = { /* navigator.push(RegisterScreen()) */ }
        )
    }
}

@Composable
fun LoginScreenContent(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Вход", fontSize = 32.sp, color = TrackMePurple)

            Spacer(modifier = Modifier.height(40.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Забыли пароль? Восстановить?",
                style = MaterialTheme.typography.bodySmall, // Тот же стиль, что у "Зарегистрироваться"
                color = TrackMePurple,
                modifier = Modifier.clickable {
                    // В будущем: navigator.push(RecoveryScreen())
                }
            )

            Spacer(modifier = Modifier.height(9.dp))

            TrackMeButton(
                text = "Войти",
                onClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Зарегистрироваться",
                modifier = Modifier.clickable { onRegisterClick() },
                color = TrackMePurple
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    LoginScreenContent(
        username = "ivan_ivanov",
        password = "123",
        onUsernameChange = {},
        onPasswordChange = {},
        onLoginClick = {},
        onRegisterClick = {}
    )
}