package com.example.track_me_mobile.features.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.ui.theme.TrackMeDeepPurple
import com.example.track_me_mobile.features.auth.presentation.LoginScreen
import com.example.track_me_mobile.features.streams.presentation.StreamListScreen
import com.example.track_me_mobile.features.teams.presentation.TeamListScreen

class SplashScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<SplashViewModel>()

        LaunchedEffect(Unit) {
            viewModel.checkAuth { destination ->
                when (destination) {
                    SplashDestination.Login -> navigator.replaceAll(LoginScreen())
                    SplashDestination.AdminHome -> navigator.replaceAll(StreamListScreen())
                    SplashDestination.TrackerHome -> navigator.replaceAll(TeamListScreen())
                }
            }
        }

        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "TrackMe",
                    style = MaterialTheme.typography.displayMedium,
                    color = TrackMeDeepPurple,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                CircularProgressIndicator(color = TrackMeDeepPurple)

                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

enum class SplashDestination {
    Login,
    AdminHome,
    TrackerHome
}