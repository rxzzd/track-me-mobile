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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCreateScreen(
    onBackClick: () -> Unit,
    onNavigateToInfo: (TeamFilterData) -> Unit
) {
    var showFilter by remember { mutableStateOf(false) }
    var teamData by remember { mutableStateOf(TeamFilterData()) }
    var descriptionText by remember { mutableStateOf("") }

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
                    // Жестко фиксируем цвета шапки
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TrackMePurple,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            containerColor = BackgroundWhite
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding() // Отступ для клавиатуры
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Стрелка назад - всегда фиолетовая
                Text(
                    text = "←",
                    color = TrackMePurple,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onBackClick() }
                )

                Spacer(modifier = Modifier.height(24.dp))
                TrackerRow()
                Spacer(modifier = Modifier.height(20.dp))

                // Секции выбора (теперь кнопки стоят как надо)
                AddSectionRow(label = "Поток:", value = teamData.stream, onAddClick = { showFilter = true })
                Spacer(modifier = Modifier.height(16.dp))

                val marketsLabel = if (teamData.markets.isNotEmpty()) "${teamData.markets.size} выбрано" else ""
                AddSectionRow(label = "Рынки НТИ:", value = marketsLabel, onAddClick = { showFilter = true })
                Spacer(modifier = Modifier.height(16.dp))

                AddSectionRow(label = "TRL:", value = teamData.trl, onAddClick = { showFilter = true })

                Spacer(modifier = Modifier.height(24.dp))

                // ПОЛЕ ВВОДА С ФИКСИРОВАННЫМИ ЦВЕТАМИ
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    textStyle = TextStyle(
                        color = Color.Black, // Основной цвет текста
                        fontSize = 14.sp
                    ),
                    placeholder = {
                        Text("Описание карточки команд...", color = TextGray)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        // Текст внутри поля (фокус и без)
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,

                        // Цвет рамки
                        focusedBorderColor = TrackMePurple,
                        unfocusedBorderColor = TrackMePurple, // Убрал alpha, чтобы не было "прозрачности"

                        // Цвета контейнера (делаем всегда прозрачным, чтобы фон не белел)
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,

                        // Курсор
                        cursorColor = TrackMePurple,

                        // Это отключит системное изменение цвета при ошибках или наведении
                        errorTextColor = Color.Black,
                        disabledTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = {
                            // Создаем копию данных с введенным описанием
                            val finalData = teamData.copy(description = descriptionText)
                            onNavigateToInfo(finalData)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                        modifier = Modifier.width(200.dp).height(48.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Создать", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AddSectionRow(label: String, value: String, onAddClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
        // Убрали fillMaxWidth(), чтобы элементы не разлетались
    ) {
        // Текст заголовка - всегда черный
        Text(text = label, fontSize = 14.sp, color = Color.Black)

        Spacer(modifier = Modifier.width(16.dp))

        // Значение (если выбрано)
        if (value.isNotEmpty()) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = TrackMePurple,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Кнопка "+" стоит сразу за текстом
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