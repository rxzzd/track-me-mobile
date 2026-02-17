package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMeetingsScreen(
    meetings: List<MeetingData>,
    onBackClick: () -> Unit,
    onPlanMeetingClick: () -> Unit
) {
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
                .padding(16.dp)
        ) {
            // Заголовок со стрелкой назад
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "←",
                    color = TrackMePurple,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Встречи команды",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrackMePurple
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Основная карточка-контейнер
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, TrackMePurple, RoundedCornerShape(28.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Контентная область (список или заглушка)
                    Box(
                        modifier = Modifier
                            .weight(1f) // Занимает всё доступное пространство, толкая кнопку вниз
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (meetings.isEmpty()) {
                            Text(
                                "У данной команды еще не было встреч",
                                color = Color.Gray,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 40.dp)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(meetings) { meeting ->
                                    MeetingItemRow(meeting)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка Запланировать (теперь всегда внизу)
                    Button(
                        onClick = onPlanMeetingClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                    ) {
                        Text("Запланировать", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingItemRow(meeting: MeetingData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TrackMePurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = meeting.date,
            color = TrackMePurple,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(12.dp))

        // Вертикальный разделитель (Material 3)
        VerticalDivider(
            modifier = Modifier.height(20.dp),
            thickness = 1.dp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = meeting.title,
            color = Color.Black,
            fontSize = 14.sp
        )
    }
}