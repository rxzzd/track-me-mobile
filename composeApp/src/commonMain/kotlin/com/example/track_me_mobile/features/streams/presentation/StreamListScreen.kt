package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.example.track_me_mobile.core.ui.theme.TrackMeDeepPurple

class StreamListScreen : Screen {
    @Composable
    override fun Content() {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFE3F2FD)) { // Светло-голубой
            Box(contentAlignment = Alignment.Center) {
                Text("ЭКРАН ПОТОКОВ (ADMIN)", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}