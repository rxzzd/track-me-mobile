package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.features.profile.presentation.components.ProfileTopHeader
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingEditScreen(
    initialTasks: String,
    initialStatus: String,
    initialLink: String,
    initialDate: String, // Добавили параметр
    onSave: (String, String, String, String) -> Unit, // Теперь 4 параметра
    onBack: () -> Unit
) {
    var tasks by remember { mutableStateOf(initialTasks) }
    var status by remember { mutableStateOf(initialStatus) }
    var link by remember { mutableStateOf(initialLink) }
    var isExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedDateText by remember { mutableStateOf(initialDate) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
                        selectedDateText = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK", color = TrackMePurple) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(topBar = { ProfileTopHeader() }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TrackMePurple) }
            Text("Редактирование", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TrackMePurple)

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                Text("Дата: ", color = Color.Gray, fontSize = 14.sp)
                Surface(onClick = { showDatePicker = true }, color = TrackMePurple, shape = RoundedCornerShape(50)) {
                    Text(selectedDateText, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp), fontSize = 12.sp)
                }
            }

            MeetingInputRow("Задачи к следующей встрече:", tasks, { tasks = it }, isEnabled = true)

            Text("Текущий статус команды:", color = Color.Gray, fontSize = 13.sp)
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).border(2.dp, TrackMePurple, RoundedCornerShape(15.dp)).clip(RoundedCornerShape(15.dp)).background(Color.White)
            ) {
                val headerBg = if (isExpanded) Color.White else when (status) {
                    "Всё ок" -> Color(0xFF6DB371)
                    "Есть проблемы" -> Color(0xFFE5D170)
                    "Есть большие проблемы" -> Color(0xFFD36D6D)
                    else -> Color(0xFFC4B2F5)
                }

                Box(modifier = Modifier.fillMaxWidth().height(46.dp).background(headerBg).clickable { isExpanded = !isExpanded }, contentAlignment = Alignment.Center) {
                    Text(text = if (isExpanded) "Статус команды" else status, color = if (isExpanded || status == "Есть проблемы") Color.Black else Color.White, fontWeight = FontWeight.Medium)
                    Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = if (isExpanded) TrackMePurple else Color.White, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp))
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        StatusMenuItem("Всё ок", Color(0xFF6DB371)) { status = "Всё ок"; isExpanded = false }
                        StatusMenuItem("Есть проблемы", Color(0xFFE5D170)) { status = "Есть проблемы"; isExpanded = false }
                        StatusMenuItem("Есть большие проблемы", Color(0xFFD36D6D)) { status = "Есть большие проблемы"; isExpanded = false }
                    }
                }
            }

            // Поле скриншота
            Text("Скриншот встречи:", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(25.dp)).clickable { }, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ArrowUpward, null, tint = TrackMePurple, modifier = Modifier.size(32.dp))
                    Text("Загрузить скриншот", color = TrackMePurple, fontSize = 12.sp)
                }
            }

            MeetingInputRow("Ссылка на материалы:", link, { link = it }, isEnabled = true)

            Button(
                onClick = { onSave(tasks, link, status, selectedDateText) },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 32.dp).fillMaxWidth(0.6f).height(46.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple)
            ) { Text("Сохранить", color = Color.White) }
        }
    }
}