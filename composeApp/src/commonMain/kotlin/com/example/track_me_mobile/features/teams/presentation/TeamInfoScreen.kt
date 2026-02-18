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
    viewModel: TeamViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onMeetingsClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    // Подписка на StateFlow — экран перерисуется автоматически при любом изменении
    val data by viewModel.teamData.collectAsState()

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "←",
                color = TrackMePurple,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoTextRow(label = "Название потока:", value = data.stream.ifEmpty { "Не указано" }, isPurple = true)
            InfoTextRow(label = "Кол-во команд:", value = "12 команд", isPurple = true)
            InfoTextRow(label = "Дата проведения:", value = "01.01.2025 - 10.10.2025", isPurple = true)

            Spacer(modifier = Modifier.height(24.dp))

            TrackerRow(name = data.trackerName) // только отображение, без редактирования

            Spacer(modifier = Modifier.height(16.dp))

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

            Text("TRL:", fontSize = 14.sp, color = TextBlack)
            Spacer(modifier = Modifier.height(8.dp))
            PurpleChip(text = data.trl.ifEmpty { "Не указано" })

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TrackMePurple, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = data.description.ifEmpty { "Описание отсутствует" },
                    color = TextBlack,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onEditClick, modifier = Modifier.height(45.dp)) {
                    Text(
                        text = "Редактировать",
                        color = TrackMePurple,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onMeetingsClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(45.dp).width(150.dp)
                ) {
                    Text("Встречи", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
