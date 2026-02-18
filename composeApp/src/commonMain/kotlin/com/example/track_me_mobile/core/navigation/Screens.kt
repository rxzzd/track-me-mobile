package com.example.track_me_mobile.core.navigation

import cafe.adriel.voyager.core.screen.Screen
import androidx.compose.runtime.Composable
import com.example.track_me_mobile.features.tracker_list.presentation.TrackerListScreen as TrackerListContent
import com.example.track_me_mobile.features.profile.presentation.ProfileScreen as ProfileContent

// Объект для списка трекеров
class TrackerListScreen : Screen {
    @Composable
    override fun Content() {
        TrackerListContent() // Вызываем твой Composable из features
    }
}

