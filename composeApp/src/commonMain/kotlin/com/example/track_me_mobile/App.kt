package com.example.track_me_mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.track_me_mobile.core.navigation.TrackerListScreen

@Composable
fun App() {
    MaterialTheme {
        // Просто указываем стартовый экран. Больше никаких NavHost!
        Navigator(TrackerListScreen()) { navigator ->
            SlideTransition(navigator) // Добавляет красивую анимацию перехода
        }
    }
}