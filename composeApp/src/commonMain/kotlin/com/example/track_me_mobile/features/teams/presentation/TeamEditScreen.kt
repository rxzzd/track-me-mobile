package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.track_me_mobile.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamEditScreen(
    initialData: TeamFilterData, // Получает текущие данные из InfoScreen
    onBackClick: () -> Unit,
    onSaveClick: (TeamFilterData) -> Unit, // Передает измененный объект назад
    onDeactivateClick: () -> Unit
) {
    var showFilter by remember { mutableStateOf(false) }
    var teamData by remember { mutableStateOf(initialData) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showFilter) {
        TeamFilterScreen(
            currentData = teamData,
            onClose = { showFilter = false },
            onApply = { newData ->
                // Сохраняем описание при смене фильтров
                teamData = newData.copy(description = teamData.description)
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
                Text(
                    text = "←",
                    color = TrackMePurple,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onBackClick() }
                )

                Spacer(modifier = Modifier.height(16.dp))
                TrackerRow()
                Spacer(modifier = Modifier.height(16.dp))

                InfoTextRow(
                    label = "Поток:",
                    value = teamData.stream.ifEmpty { "Название потока" },
                    isPurple = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Рынки НТИ:", fontSize = 14.sp, color = TextBlack)
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
                    Text("TRL: ", fontSize = 14.sp, color = TextBlack)
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

                OutlinedTextField(
                    value = teamData.description,
                    onValueChange = { newDesc ->
                        teamData = teamData.copy(description = newDesc)
                    },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                    placeholder = { Text("Описание карточки команд...", color = Color.Gray) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = TrackMePurple,
                        unfocusedBorderColor = TrackMePurple.copy(alpha = 0.5f),
                        cursorColor = TrackMePurple,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onSaveClick(teamData) }, // Возвращаем измененные данные
                        colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Сохранить", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3524E)),
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Деактивировать", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }



    // ДИАЛОГ ПОДТВЕРЖДЕНИЯ ДЕАКТИВАЦИИ
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            content = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(4.dp, Color(0xFFD3524E), RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = { showDeleteDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFD3524E))
                            }
                        }

                        Text(
                            text = "Вы уверены, что хотите остановить работу данной команды?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Кнопка Нет
                            Button(
                                onClick = { showDeleteDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3524E)),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f).height(50.dp).padding(horizontal = 8.dp)
                            ) {
                                Text("Нет", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            // Кнопка Да
                            Button(
                                onClick = {
                                    showDeleteDialog = false
                                    onDeactivateClick()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f).height(50.dp).padding(horizontal = 8.dp)
                            ) {
                                Text("Да", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TeamEditScreenPreview() {
    val sampleData = TeamFilterData(
        stream = "Информационные технологии",
        markets = listOf("HealthNet", "TechNet"),
        trl = "6-8",
        description = "Текст описания, который можно редактировать"
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