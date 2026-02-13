package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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

// --- Модель данных (хранит выбор пользователя) ---
data class TeamFilterData(
    val stream: String = "",
    val markets: List<String> = emptyList(),
    val trl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCreateScreen(onBackClick: () -> Unit = {}) {
    // --- Состояния ---
    var showFilter by remember { mutableStateOf(false) } // Открыт ли фильтр?
    var isViewMode by remember { mutableStateOf(false) } // Режим просмотра (после нажатия "Создать")

    // Здесь храним данные, которые выбрали в фильтре
    var teamData by remember { mutableStateOf(TeamFilterData()) }

    // --- Логика переключения экранов ---
    if (showFilter) {
        // Показываем экран фильтров
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

            // Если нажали "Создать" (isViewMode == true), показываем инфо-карточку
            if (isViewMode) {
                TeamInfoView(
                    padding = padding,
                    data = teamData,
                    onBackClick = onBackClick,
                    onEditClick = { isViewMode = false } // Вернуться к редактированию
                )
            } else {
                // Иначе показываем форму создания
                TeamCreationForm(
                    padding = padding,
                    data = teamData,
                    onBackClick = onBackClick,
                    onAddFilterClick = { showFilter = true },
                    onCreateClick = { isViewMode = true } // Переходим в режим просмотра
                )
            }
        }
    }
}

// --- Экран 1: Форма создания (с плюсиками) ---
@Composable
fun TeamCreationForm(
    padding: PaddingValues,
    data: TeamFilterData,
    onBackClick: () -> Unit,
    onAddFilterClick: () -> Unit,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Стрелка назад
        Text("←", color = TrackMePurple, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBackClick() })
        Spacer(modifier = Modifier.height(24.dp))

        // Поле Трекер
        TrackerRow()
        Spacer(modifier = Modifier.height(20.dp))

        // Строки с плюсиками (или выбранными значениями)
        AddSectionRow(label = "Поток:", value = data.stream, onAddClick = onAddFilterClick)
        Spacer(modifier = Modifier.height(16.dp))

        val marketsLabel = if (data.markets.isNotEmpty()) "${data.markets.size} выбрано" else ""
        AddSectionRow(label = "Рынки НТИ:", value = marketsLabel, onAddClick = onAddFilterClick)
        Spacer(modifier = Modifier.height(16.dp))

        AddSectionRow(label = "TRL:", value = data.trl, onAddClick = onAddFilterClick)

        Spacer(modifier = Modifier.height(24.dp))

        // Поле для описания
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

        // Кнопка "Создать"
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                modifier = Modifier.width(200.dp).height(45.dp),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text("Создать", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// --- Экран 2: Просмотр (Результат) ---
@Composable
fun TeamInfoView(
    padding: PaddingValues,
    data: TeamFilterData,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("←", color = TrackMePurple, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBackClick() })
        Spacer(modifier = Modifier.height(16.dp))

        // Блок информации
        // Данные берутся из сохраненного data.stream. Дата и кол-во - пока заглушки.
        InfoTextRow(label = "Название потока:", value = data.stream.ifEmpty { "Не указано" }, isPurple = true)
        InfoTextRow(label = "Кол-во команд:", value = "12 команд", isPurple = true)
        InfoTextRow(label = "Дата проведения:", value = "01.01.2025 - 10.10.2025", isPurple = true)

        Spacer(modifier = Modifier.height(24.dp))

        TrackerRow()

        Spacer(modifier = Modifier.height(16.dp))

        // Рынки НТИ (отображаем фиолетовыми чипсами)
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

        // Описание
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TrackMePurple, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Описание карточки команд... Описание карточки команд... Описание карточки команд...",
                color = TextBlack,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Нижние кнопки
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onEditClick) {
                Text("Редактировать", color = TrackMePurple, fontSize = 16.sp)
            }

            Button(
                onClick = { /* Логика для кнопки Встречи */ },
                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(45.dp).width(150.dp)
            ) {
                Text("Встречи", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Компоненты UI ---

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
fun AddSectionRow(label: String, value: String, onAddClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = TextBlack)
        Spacer(modifier = Modifier.width(16.dp))

        // Если значение есть, показываем текст, иначе - кнопку +
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
@Preview
@Composable
fun TeamCreateScreen() {
    MaterialTheme {
        TeamCreateScreen()
    }
}