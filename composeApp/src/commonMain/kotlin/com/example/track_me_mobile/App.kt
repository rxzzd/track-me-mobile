package com.example.track_me_mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.track_me_mobile.features.auth.presentation.LoginScreen
import com.example.track_me_mobile.features.profile.presentation.ProfileScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        ProfileScreen()

    }
}