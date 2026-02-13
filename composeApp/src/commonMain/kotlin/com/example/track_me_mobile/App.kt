package com.example.track_me_mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import org.koin.compose.KoinContext
import com.example.track_me_mobile.core.ui.theme.TrackMeTypography
import com.example.track_me_mobile.core.ui.theme.TrackMeDeepPurple
import com.example.track_me_mobile.features.auth.presentation.LoginScreen

@Composable
fun App() {
    // Настраиваем цветовую схему Material 3
    val trackMeColorScheme = lightColorScheme(
        primary = TrackMeDeepPurple,
        background = Color.White,
        surface = Color.White,
        onPrimary = Color.White,
        onBackground = Color.Black
    )

    MaterialTheme(
        typography = TrackMeTypography(), // Наш Montserrat
        colorScheme = trackMeColorScheme
    ) {
        // Оборачиваем в контекст Koin для Dependency Injection
        KoinContext {
            // Навигатор Voyager
            Navigator(screen = LoginScreen()) { navigator ->
                // Добавляем анимацию переходов (Slide)
                SlideTransition(navigator)
            }
        }
    }
}