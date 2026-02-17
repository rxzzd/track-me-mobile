package com.example.track_me_mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.track_me_mobile.features.teams.presentation.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
@Composable
fun App() {
    MaterialTheme {
        Navigator(screen = CreateTeamLevel()) { navigator ->
            SlideTransition(navigator)
        }
    }
}