package com.example.track_me_mobile.features.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// ТЕПЕРЬ ЭТИ ИМПОРТЫ БУДУТ РАБОТАТЬ
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import com.example.track_me_mobile.core.ui.theme.TrackMePurple

@Composable
fun ProfileTopHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(TrackMePurple)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("T", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text("TrackMe", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }

        // Кнопка меню справа
        IconButton(
            onClick = { /* Здесь будет открытие меню */ },
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Меню",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}