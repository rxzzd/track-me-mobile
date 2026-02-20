package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.features.profile.presentation.components.ProfileTopHeader
import com.example.track_me_mobile.features.streams.presentation.components.*

data class StreamData(val id: Int, val name: String, val tracker: String, val date: String)

@Composable
fun StreamsListScreen() {
    val vertState = rememberScrollState()
    val horizState = rememberScrollState()
    var selectedTracker by remember { mutableStateOf("Все") }
    var selectedStreamName by remember { mutableStateOf("Все") }

    val allStreams = remember {
        listOf(
            StreamData(1, "ИТ Поток", "Иванов И.И.", "01.09.25"),
            StreamData(2, "Маркетинг", "Петров П.П.", "10.09.25"),
            StreamData(3, "Технологии", "Иванов И.И.", "15.09.25")
        ) + (4..15).map { StreamData(it, "Поток №$it", "Александров А.А.", "12.10.25") }
    }

    val trackerOptions = listOf("Все", "Александров А.А.", "Иванов И.И.", "Петров П.П.")
    val streamOptions = listOf("Все") + allStreams.map { it.name }.distinct()

    val filteredStreams = allStreams.filter { stream ->
        (selectedTracker == "Все" || stream.tracker == selectedTracker) &&
                (selectedStreamName == "Все" || stream.name == selectedStreamName)
    }

    Scaffold(topBar = { ProfileTopHeader() }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            OutlinedButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 12.dp),
                border = BorderStroke(2.dp, TrackMePurple),
                shape = RoundedCornerShape(50)
            ) { Text("Выгрузить отчет", color = TrackMePurple, fontWeight = FontWeight.Bold) }

            // Поля выбора теперь используют zIndex для корректного наложения Popup
            Box(modifier = Modifier.zIndex(2f)) {
                FilterDropdownMenu("Трекеры", selectedTracker, trackerOptions) { selectedTracker = it }
            }
            Box(modifier = Modifier.zIndex(1f)) {
                FilterDropdownMenu("Потоки", selectedStreamName, streamOptions) { selectedStreamName = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth().border(2.dp, TrackMePurple)) {
                Row(modifier = Modifier.horizontalScroll(horizState)) {
                    Column(modifier = Modifier.verticalScroll(vertState).width(600.dp)) {
                        Row(modifier = Modifier.background(Color(0xFFD1C4E9))) {
                            TableCell("№", 50.dp, true)
                            TableCell("Название", 250.dp, true)
                            TableCell("Трекер", 200.dp, true)
                            TableCell("Дата", 100.dp, true)
                        }
                        filteredStreams.forEach { stream ->
                            Row {
                                TableCell(stream.id.toString(), 50.dp)
                                TableCell(stream.name, 250.dp)
                                TableCell(stream.tracker, 200.dp)
                                TableCell(stream.date, 100.dp)
                            }
                        }
                    }
                }
                CustomVerticalScrollbar(vertState, Modifier.align(Alignment.CenterEnd).padding(top = 44.dp))
                CustomHorizontalScrollbar(horizState, Modifier.align(Alignment.BottomCenter))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FilterDropdownMenu(title: String, current: String, items: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val displayText = if (current == "Все") title else current

    // Используем Box для позиционирования Popup относительно кнопки
    Box(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { expanded = !expanded } // Теперь этот клик сработает всегда
                .border(
                    2.dp, TrackMePurple,
                    if (expanded) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    else RoundedCornerShape(50.dp)
                ),
            shape = if (expanded) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            else RoundedCornerShape(50.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(displayText, color = TrackMePurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = TrackMePurple
                )
            }
        }

        if (expanded) {
            Popup(
                // ВАЖНО: Смещаем Popup вниз на высоту кнопки (48dp),
                // чтобы он не перекрывал саму кнопку и она могла поймать клик для закрытия
                offset = androidx.compose.ui.unit.IntOffset(0, with(androidx.compose.ui.platform.LocalDensity.current) { 48.dp.roundToPx() }),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
            ) {
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { isVisible = true }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            // Убираем padding(top = 46.dp), так как мы уже сместили весь Popup через offset
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .background(Color.White, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                            .border(2.dp, TrackMePurple, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
                                items.forEachIndexed { i, item ->
                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (i % 2 == 0) Color.White else Color(0xFFE8E0FF))
                                        .clickable {
                                            onSelect(item)
                                            expanded = false
                                        }
                                        .padding(16.dp, 12.dp)
                                    ) { Text(item, fontSize = 13.sp, color = Color.Black) }
                                }
                            }
                            Column(
                                modifier = Modifier.width(24.dp).fillMaxHeight().padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, null, tint = TrackMePurple, modifier = Modifier.size(16.dp))
                                CustomVerticalScrollbar(scrollState, Modifier.weight(1f).padding(vertical = 2.dp))
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = TrackMePurple, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}