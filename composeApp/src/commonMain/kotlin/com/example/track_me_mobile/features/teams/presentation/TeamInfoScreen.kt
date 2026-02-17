package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamInfoScreen(
    data: TeamFilterData, // Данные, которые приходят из Create или Edit экрана
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onMeetingsClick: () -> Unit
) {
    // Используем Scaffold для базовой разметки (шапка + фон)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TrackMe", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TrackMePurple)
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Чтобы экран прокручивался, если текста много
                .padding(16.dp)
        ) {
            // Кнопка возврата (стрелка)
            Text(
                text = "←",
                color = TrackMePurple,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Основная информация о потоке
            InfoTextRow(
                label = "Название потока:",
                value = data.stream.ifEmpty { "Не указано" },
                isPurple = true
            )
            InfoTextRow(label = "Кол-во команд:", value = "12 команд", isPurple = true)
            InfoTextRow(label = "Дата проведения:", value = "01.01.2025 - 10.10.2025", isPurple = true)

            Spacer(modifier = Modifier.height(24.dp))

            // Компонент трекера (из вашего TeamComponents)
            TrackerRow()

            Spacer(modifier = Modifier.height(16.dp))

            // Секция Рынков НТИ
            Text("Рынки НТИ:", fontSize = 14.sp, color = TextBlack)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                if (data.markets.isEmpty()) {
                    PurpleChip(text = "Нет рынков")
                } else {
                    data.markets.forEach { market ->
                        PurpleChip(text = market)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TRL
            Text("TRL:", fontSize = 14.sp, color = TextBlack)
            Spacer(modifier = Modifier.height(8.dp))
            PurpleChip(text = data.trl.ifEmpty { "Не указано" })

            Spacer(modifier = Modifier.height(24.dp))

            // --- БЛОК ОПИСАНИЯ (Здесь отображается созданный/отредактированный текст) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TrackMePurple, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    // Мы берем описание напрямую из объекта 'data'.
                    // Когда навигация обновляет этот объект, Compose автоматически перерисует текст.
                    text = data.description.ifEmpty { "Описание отсутствует" },
                    color = TextBlack,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            // Пружина (Spacer с весом), чтобы кнопки всегда были внизу
            Spacer(modifier = Modifier.height(40.dp))

            // --- НИЖНИЙ БЛОК КНОПОК ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Кнопка Редактировать
                TextButton(
                    onClick = onEditClick,
                    modifier = Modifier.height(45.dp)
                ) {
                    Text(
                        text = "Редактировать",
                        color = TrackMePurple,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Кнопка Встречи
                Button(
                    onClick = onMeetingsClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(45.dp)
                        .width(150.dp)
                ) {
                    Text("Встречи", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}