package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.animation.*
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
import com.example.track_me_mobile.features.meetings.presentation.components.*
import com.example.track_me_mobile.features.profile.presentation.components.ProfileTopHeader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingEditScreen(
    initialTasks: String,
    initialTeamInfo: String,
    initialStatus: String,
    initialLinkRecord: String,
    initialLinkVideo: String,
    initialDate: String,
    initialMeetingResult: String,
    initialScreenshot: String?, // Добавлено
    onSave: (String, String, String, String, String, String, String, String?) -> Unit, // Теперь 8 параметров
    onBack: () -> Unit
) {
    var tasks by remember { mutableStateOf(initialTasks) }
    var teamInfo by remember { mutableStateOf(initialTeamInfo) }
    var status by remember { mutableStateOf(initialStatus) }
    var linkRecord by remember { mutableStateOf(initialLinkRecord) }
    var linkVideo by remember { mutableStateOf(initialLinkVideo) }
    var selectedDateText by remember { mutableStateOf(initialDate) }
    var meetingResult by remember { mutableStateOf(initialMeetingResult) }
    var screenshotUri by remember { mutableStateOf(initialScreenshot) } // Добавлено
    var isExpanded by remember { mutableStateOf(false) }

    // Твоя оригинальная проверка даты
    val isDatePassed = remember(selectedDateText) {
        try {
            val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
            val meetingDate = sdf.parse(selectedDateText)
            val today = Calendar.getInstance()
            val todayDate = sdf.parse("${today.get(Calendar.DAY_OF_MONTH)}.${today.get(Calendar.MONTH) + 1}")

            meetingDate != null && !meetingDate.after(todayDate)
        } catch (e: Exception) {
            false
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TrackMePurple) }
                Text("Редактирование", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TrackMePurple)
            }

            // Твои кнопки Состоялась / Не состоялась
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val isDoneSelected = meetingResult == "Состоялась"
                val isFailedSelected = meetingResult == "Не состоялась"

                Button(
                    onClick = { if (isDatePassed) meetingResult = "Состоялась" },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDatePassed) {
                            if (isDoneSelected) Color(0xFF6DB371) else Color(0xFF99C9A3)
                        } else Color.LightGray.copy(alpha = 0.4f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50)
                ) { Text("Состоялась") }

                Button(
                    onClick = { if (isDatePassed) meetingResult = "Не состоялась" },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDatePassed) {
                            if (isFailedSelected) Color(0xFFD36D6D) else Color(0xFFD5938D)
                        } else Color.LightGray.copy(alpha = 0.4f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50)
                ) { Text("Не состоялась") }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Дата: ", color = Color.Black)
                Surface(onClick = { showDatePicker = true }, color = TrackMePurple, shape = RoundedCornerShape(50)) {
                    Text(selectedDateText, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }

            MeetingInputRow("Задачи к следующей встрече:", tasks, { tasks = it }, isEnabled = true)
            Spacer(Modifier.height(8.dp))
            MeetingInputRow("Информация о команде:", teamInfo, { teamInfo = it }, isEnabled = true)

            // Твой выпадающий список статуса
            Text("Текущий статус команды:", color = Color.Black, modifier = Modifier.padding(top = 16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(2.dp, TrackMePurple, RoundedCornerShape(25.dp))
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color.White)
            ) {
                val headerBg = if (isExpanded) Color.White else when (status) {
                    "Всё ок" -> Color(0xFF6DB371)
                    "Есть проблемы" -> Color(0xFFE5D170)
                    else -> Color(0xFFD36D6D)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(headerBg)
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isExpanded) "Выберите статус..." else status,
                        color = if (isExpanded || status == "Есть проблемы") Color.Black else Color.White
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isExpanded) TrackMePurple else Color.White
                    )
                }
                AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column {
                        StatusMenuItem("Всё ок", Color(0xFF6DB371)) { status = "Всё ок"; isExpanded = false }
                        StatusMenuItem("Есть проблемы", Color(0xFFE5D170)) { status = "Есть проблемы"; isExpanded = false }
                        StatusMenuItem("Есть большие проблемы", Color(0xFFD36D6D)) { status = "Есть большие проблемы"; isExpanded = false }
                    }
                }
            }

            // СКРИНШОТ СРАЗУ ПОСЛЕ СТАТУСА
            ScreenshotPickerBlock(
                screenshotUri = screenshotUri,
                isEditing = true,
                onUploadClick = { screenshotUri = "dummy_path" }
            )

            Spacer(modifier = Modifier.height(16.dp))
            MeetingInputRow("Запись встречи (ссылка):", linkRecord, { linkRecord = it }, isEnabled = true)
            Spacer(Modifier.height(8.dp))
            MeetingInputRow("Ссылка на видеовстречу:", linkVideo, { linkVideo = it }, isEnabled = true)

            Spacer(modifier = Modifier.height(32.dp))

            // Твоя кнопка сохранения (теперь передает screenshotUri)
            Button(
                onClick = { onSave(tasks, teamInfo, linkRecord, linkVideo, status, selectedDateText, meetingResult, screenshotUri) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Сохранить изменения", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}