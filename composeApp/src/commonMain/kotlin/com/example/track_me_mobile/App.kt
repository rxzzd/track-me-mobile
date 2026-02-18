package com.example.track_me_mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.track_me_mobile.features.meetings.presentation.MeetingNavigation

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Вызываем навигатор, который сам решит, какой экран показать
        // В файле App.kt
        MeetingNavigation(onBack = {
            // Здесь логика возврата, например: navController.popBackStack(
        })
    }
}