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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamEditScreen(
    initialData: TeamFilterData,
    onBackClick: () -> Unit,
    onSaveClick: (TeamFilterData) -> Unit,
    onDeactivateClick: () -> Unit
) {
    var showFilter by remember { mutableStateOf(false) }
    var teamData by remember { mutableStateOf(initialData) }

    if (showFilter) {
        TeamFilterScreen(
            currentData = teamData,
            onClose = { showFilter = false },
            onApply = { newData ->
                teamData = newData
                showFilter = false
            },
            onReset = { teamData = TeamFilterData() }
        )
    } else {
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Стрелка назад
                Text(
                    text = "←",
                    color = TrackMePurple,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onBackClick() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поля редактирования (используем компоненты из TeamComponents)
                TrackerRow()
                Spacer(modifier = Modifier.height(16.dp))

                InfoTextRow(
                    label = "Поток:",
                    value = teamData.stream.ifEmpty { "Название потока" },
                    isPurple = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Рынки НТИ:", fontSize = 14.sp)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    if (teamData.markets.isEmpty()) {
                        PurpleChip("Не выбрано")
                    } else {
                        teamData.markets.forEach { market ->
                            PurpleChip(market)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("TRL: ", fontSize = 14.sp)
                    PurpleChip(teamData.trl.ifEmpty { "TRL" })
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Фильтр",
                        color = TrackMePurple,
                        modifier = Modifier.clickable { showFilter = true },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Описание
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(1.dp, TrackMePurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Описание карточки команд... (здесь текст описания)",
                        color = TextBlack,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Кнопки Сохранить и Деактивировать
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onSaveClick(teamData) },
                        colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Сохранить", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onDeactivateClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3524E)), // Тот самый красный
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Деактивировать", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// --- ПРЕВЬЮ ДЛЯ ПРОВЕРКИ ---
@Preview(showBackground = true)
@Composable
fun TeamEditScreenPreview() {
    val sampleData = TeamFilterData(
        stream = "Название потока 1",
        markets = listOf("AutoNet", "HealthNet", "SafeNet"),
        trl = "6-8"
    )

    MaterialTheme {
        TeamEditScreen(
            initialData = sampleData,
            onBackClick = {},
            onSaveClick = {},
            onDeactivateClick = {}
        )
    }
}