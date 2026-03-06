package com.example.track_me_mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import org.koin.compose.KoinApplication
import com.example.track_me_mobile.core.di.appModule
import com.example.track_me_mobile.core.ui.theme.TrackMeTypography
import com.example.track_me_mobile.core.ui.theme.TrackMeDeepPurple
import com.example.track_me_mobile.features.auth.presentation.LoginScreen

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        AppContent()
    }
}

@Composable
private fun AppContent() {
    // Получаем HttpClient из Koi

    val trackMeColorScheme = lightColorScheme(
        primary = TrackMeDeepPurple,
        background = Color.White,
        surface = Color.White,
        onPrimary = Color.White,
        onBackground = Color.Black,
        error = Color(0xFFBA1A1A)
    )

    MaterialTheme(
        typography = TrackMeTypography(),
        colorScheme = trackMeColorScheme
    ) {
        Navigator(screen = LoginScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}