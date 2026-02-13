package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun TeamCreateScreen(
    onBackClick: () -> Unit,
    onNavigateToInfo: (TeamFilterData) -> Unit // Переход на экран просмотра
) {
    var showFilter by remember { mutableStateOf(false) }
    var teamData by remember { mutableStateOf(TeamFilterData()) }

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
                Text("←", color = TrackMePurple, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBackClick() })
                Spacer(modifier = Modifier.height(24.dp))

                TrackerRow() // Из TeamComponents.kt
                Spacer(modifier = Modifier.height(20.dp))

                // Кнопки выбора
                AddSectionRow(label = "Поток:", value = teamData.stream, onAddClick = { showFilter = true })
                Spacer(modifier = Modifier.height(16.dp))

                val marketsLabel = if (teamData.markets.isNotEmpty()) "${teamData.markets.size} выбрано" else ""
                AddSectionRow(label = "Рынки НТИ:", value = marketsLabel, onAddClick = { showFilter = true })
                Spacer(modifier = Modifier.height(16.dp))

                AddSectionRow(label = "TRL:", value = teamData.trl, onAddClick = { showFilter = true })

                Spacer(modifier = Modifier.height(24.dp))

                // Поле описания
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(1.dp, TrackMePurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text("/", color = TextGray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Кнопка Создать -> Переход на следующий экран
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { onNavigateToInfo(teamData) },
                        colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                        modifier = Modifier.width(200.dp).height(45.dp),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text("Создать", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddSectionRow(label: String, value: String, onAddClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = TextBlack)
        Spacer(modifier = Modifier.width(16.dp))

        if (value.isNotEmpty() && value != "Не выбрано") {
            Text(text = value, fontSize = 14.sp, color = TrackMePurple, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(TrackMePurple)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun TeamCreateScreenPreview() {
    MaterialTheme {
        TeamCreateScreen(
            onBackClick = {},
            onNavigateToInfo = {}
        )
    }
}