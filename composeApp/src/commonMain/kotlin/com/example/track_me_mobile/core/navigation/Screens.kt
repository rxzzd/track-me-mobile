package com.example.track_me_mobile.core.navigation

import cafe.adriel.voyager.core.screen.Screen
import androidx.compose.runtime.Composable
import com.example.track_me_mobile.features.users.presentation.TrackerListScreen as TrackerListContent
import com.example.track_me_mobile.features.users.presentation.AdminListScreen as AdminListContent
// Объект для списка трекеров
class TrackerListScreen : Screen {
    @Composable
    override fun Content() {
        TrackerListContent() // Вызываем твой Composable из features
    }
}

class AdminListScreen : Screen {
    @Composable
    override fun Content() {
        AdminListContent()
    }
}

