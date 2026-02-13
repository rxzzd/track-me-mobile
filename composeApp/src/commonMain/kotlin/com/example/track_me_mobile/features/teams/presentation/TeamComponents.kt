package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*

// --- Модель данных ---
data class TeamFilterData(
    val stream: String = "",
    val markets: List<String> = emptyList(),
    val trl: String = ""
)

// --- Общие UI компоненты ---

@Composable
fun TrackerRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Трекер:", fontSize = 14.sp, color = TextBlack)
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .background(TrackMePurpleLight.copy(alpha = 0.3f), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Иванов Иван Иванович", color = TrackMePurple, fontSize = 14.sp)
        }
    }
}

@Composable
fun PurpleChip(text: String) {
    Box(
        modifier = Modifier
            .background(TrackMePurple, RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoTextRow(label: String, value: String, isPurple: Boolean = false) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label ", fontSize = 14.sp, color = TextBlack)
        Text(
            text = value,
            fontSize = 14.sp,
            color = if (isPurple) TrackMePurple else TextBlack,
            fontWeight = if (isPurple) FontWeight.Bold else FontWeight.Normal
        )
    }
}