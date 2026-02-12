package com.example.track_me_mobile.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import com.example.track_me_mobile.core.ui.theme.*
import com.example.track_me_mobile.features.profile.presentation.components.*

@Composable
@Preview
fun ProfileScreen() {
    Scaffold(
        topBar = { ProfileTopHeader() },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 34.dp, vertical = 35.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Личный кабинет",
                fontSize = 32.sp,
                color = TrackMePurple,
                modifier = Modifier.padding(bottom = 35.dp)
            )

            // Аватарка с человечком
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(TrackMePurpleLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Аватар",
                    modifier = Modifier.size(120.dp),
                    tint = TrackMePurple
                )
            }

            Spacer(modifier = Modifier.height(15.dp))
            Text("Администратор", color = TrackMePurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(15.dp))

            ProfileInfoRow("Иванов Иван Иванович")
            ProfileInfoRow("ivan@mail.ru")
            ProfileInfoRow("+7 999 999-99-99")
            ProfileInfoRow("@IVANIVAN")

            Spacer(modifier = Modifier.height(25.dp))

            // 1. Кнопка "Карточки команд" — ЖИРНЫЙ шрифт
            Button(
                onClick = { /* действие */ },
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .height(42.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrackMePurple,
                    contentColor = BackgroundWhite
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Карточки команд",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold // Сделал жирным
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Кнопка "Загрузить отчет" — ЖИРНЫЙ шрифт
            TextButton(onClick = { }) {
                Text(
                    text = "Загрузить отчет",
                    color = TrackMePurple,
                    fontWeight = FontWeight.Bold, // Сделал жирным
                    fontSize = 16.sp
                )
            }

            // 3. Кнопка "Редактировать" — ОБЫЧНЫЙ шрифт
            TextButton(onClick = { }) {
                Text(
                    text = "Редактировать",
                    color = TrackMePurple,
                    fontWeight = FontWeight.Normal, // Обычный шрифт
                    fontSize = 16.sp
                )
            }
        }
    }
}