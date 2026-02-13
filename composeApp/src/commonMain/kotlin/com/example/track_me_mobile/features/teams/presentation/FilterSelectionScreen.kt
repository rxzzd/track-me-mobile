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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
@Composable
fun TeamFilterScreen(
    currentData: TeamFilterData, // Модель из TeamComponents.kt
    onClose: () -> Unit,
    onApply: (TeamFilterData) -> Unit,
    onReset: () -> Unit
) {
    // 1. Локальные состояния для редактирования
    var selectedStream by remember { mutableStateOf(currentData.stream) }
    var selectedTrl by remember { mutableStateOf(currentData.trl) }

    // Используем remember { mutableStateListOf(...) }, чтобы Compose видел изменения в списке
    val selectedMarkets = remember {
        mutableStateListOf<String>().apply { addAll(currentData.markets) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrackMePurpleLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Кнопка закрытия
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White
                    )
                }
            }

            // Основной контент (скроллится)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- Секция: Поток ---
                FilterHeader("Поток")
                val streams = listOf("Название потока 1", "Название потока 2", "Название потока 3")
                streams.forEach { stream ->
                    FilterCheckboxRow(
                        label = stream,
                        isChecked = selectedStream == stream,
                        onCheckChanged = { if (it) selectedStream = stream }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Секция: Рынки НТИ ---
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

                // --- Секция: TRL ---
                FilterHeader("TRL")
                val trlList = listOf("0-2", "3-5", "6-8", "9-10")
                trlList.forEach { trl ->
                    FilterCheckboxRow(
                        label = trl,
                        isChecked = selectedTrl == trl,
                        onCheckChanged = { if (it) selectedTrl = trl }
                    )
                }
            }

            // Нижняя панель с кнопками
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    selectedStream = ""
                    selectedTrl = ""
                    selectedMarkets.clear()
                    onReset()
                }) {
                    Text(
                        "Сбросить",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                TextButton(onClick = {
                    // Возвращаем собранные данные назад
                    onApply(
                        TeamFilterData(
                            stream = selectedStream,
                            markets = selectedMarkets.toList(),
                            trl = selectedTrl
                        )
                    )
                }) {
                    Text(
                        "Применить",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- Вспомогательные компоненты (внутренние для этого файла) ---

// Убрали private, чтобы функции были доступны во всем пакете presentation
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
fun FilterCheckboxRow(
    label: String,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
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

// --- Превью (теперь должно работать корректно) ---
@Preview(showBackground = true)
@Composable
fun TeamFilterScreenPreview() {
    // В превью передаем "заглушки" (пустые действия)
    MaterialTheme {
        TeamFilterScreen(
            currentData = TeamFilterData(stream = "Название потока 1"),
            onClose = {},
            onApply = {},
            onReset = {}
        )
    }
}