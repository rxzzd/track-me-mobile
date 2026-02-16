package com.example.track_me_mobile.features.auth.presentation
<<<<<<< Updated upstream
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
=======

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.components.TrackMeButton
import com.example.track_me_mobile.core.ui.theme.TrackMeDeepPurple
import com.example.track_me_mobile.features.streams.presentation.StreamListScreen
import com.example.track_me_mobile.features.teams.presentation.TeamListScreen
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.network.logDebug
>>>>>>> Stashed changes


class LoginScreen : Screen{

    @Preview
    @Composable
    override fun Content() {
<<<<<<< Updated upstream
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text("Экран Авторизации (Скоро тут будет форма)")
        }
    }
}
=======
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<LoginViewModel>()

        LoginScreenContent(
            isLoading = viewModel.isLoading,
            errorMessage = viewModel.errorMessage,
            onLoginClick = {
                // 1. Очистка старых сессий
                com.example.track_me_mobile.core.network.clearWebViewCookies()

                // 2. Открытие окна SSO
                navigator.push(AuthWebViewScreen(onFinish = { capturedCookies ->
                    // 3. Закрываем WebView
                    navigator.pop()

                    logDebug("DEBUG_TAG: WebView вернул куки. Начинаем проверку роли...")

                    // 4. Синхронизация и переход
                    viewModel.loginFromWebView(capturedCookies) { role ->
                        when (role) {
                            Role.ADMIN, Role.SUPER_ADMIN -> navigator.replace(StreamListScreen())
                            Role.TRACKER -> navigator.replace(TeamListScreen())
                            else -> logDebug("DEBUG_TAG: Вход выполнен, но роль не определена")
                        }
                    }
                }))
            }
        )
    }
}

@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Scaffold(containerColor = Color.Transparent) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "TrackMe",
                    style = MaterialTheme.typography.displayMedium,
                    color = TrackMeDeepPurple,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Вход",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TrackMeDeepPurple
                )

                Spacer(modifier = Modifier.height(48.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = TrackMeDeepPurple)
                } else {
                    TrackMeButton(
                        text = "Войти через SSO",
                        onClick = onLoginClick
                    )
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
>>>>>>> Stashed changes
