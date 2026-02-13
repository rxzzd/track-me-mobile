package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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

// Основной экран фильтров
@Composable
fun TeamFilterScreen(
    currentData: TeamFilterData, // Принимаем текущие данные
    onClose: () -> Unit,
    onApply: (TeamFilterData) -> Unit, // Возвращаем новые данные
    onReset: () -> Unit
) {
    // Локальное состояние (временное, пока не нажали "Применить")
    var selectedStream by remember { mutableStateOf(currentData.stream) }
    var selectedTrl by remember { mutableStateOf(currentData.trl) }
    // Копия списка рынков для редактирования
    val selectedMarkets = remember { mutableStateListOf<String>().apply { addAll(currentData.markets) } }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrackMePurpleLight
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            // Кнопка закрытия
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
                }
            }

            // Скроллируемая область с опциями
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- Поток ---
                FilterHeader("Поток") // Вот эта функция, которой не хватало
                val streams = listOf("Название потока 1", "Название потока 2", "Название потока 3")
                streams.forEach { stream ->
                    FilterCheckboxRow(
                        label = stream,
                        isChecked = selectedStream == stream,
                        onCheckChanged = { if (it) selectedStream = stream }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Рынки НТИ ---
                FilterHeader("Рынки НТИ")
                val markets = listOf("AutoNet", "HealthNet", "MariNet", "NeuroNet", "SafeNet", "FoodNet", "TechNet", "WearNet")
                markets.chunked(2).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { market ->
                            Box(modifier = Modifier.weight(1f)) {
                                FilterCheckboxRow(
                                    label = market,
                                    isChecked = selectedMarkets.contains(market),
                                    onCheckChanged = { isChecked ->
                                        if (isChecked) selectedMarkets.add(market)
                                        else selectedMarkets.remove(market)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- TRL ---
                FilterHeader("TRL")
                listOf("0-2", "3-5", "6-8", "9-10").forEach { trl ->
                    FilterCheckboxRow(
                        label = trl,
                        isChecked = selectedTrl == trl,
                        onCheckChanged = { if (it) selectedTrl = trl }
                    )
                }
            }

            // Нижние кнопки (Сбросить / Применить)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    // Сброс локальных переменных
                    selectedStream = ""
                    selectedTrl = ""
                    selectedMarkets.clear()
                    onReset()
                }) {
                    Text("Сбросить", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(onClick = {
                    // Собираем результат и отправляем в Team_card.kt
                    onApply(TeamFilterData(selectedStream, selectedMarkets.toList(), selectedTrl))
                }) {
                    Text("Применить", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Вспомогательные функции (ОБЯЗАТЕЛЬНО ДОЛЖНЫ БЫТЬ ЗДЕСЬ) ---

@Composable
fun FilterHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun FilterCheckboxRow(label: String, isChecked: Boolean, onCheckChanged: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckChanged(!isChecked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckChanged,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.White,
                uncheckedColor = Color.White.copy(alpha = 0.6f),
                checkmarkColor = TrackMePurple
            )
        )
        Text(text = label, color = TextBlack, fontSize = 16.sp)
    }
}

// Превью для проверки
@Preview(showBackground = true)
@Composable
fun TeamFilterScreenPreview() {
    MaterialTheme {
        TeamFilterScreen(
            currentData = TeamFilterData(),
            onClose = {},
            onApply = {},
            onReset = {}
        )
    }
}