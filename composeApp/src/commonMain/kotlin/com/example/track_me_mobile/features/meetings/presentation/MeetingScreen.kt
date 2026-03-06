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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.features.meetings.domain.models.Meeting
import kotlinx.datetime.*
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import kotlin.time.Clock
import androidx.compose.material.icons.filled.Info

class MeetingScreen(
    private val meetingId: String,
    private val teamCardId: String  // ← ДОБАВИТЬ
) : Screen {

    @Composable
    override fun Content() {
        println("[MeetingScreen] Creating screen for meetingId=$meetingId, teamCardId=$teamCardId")

        val navigator = LocalNavigator.currentOrThrow

        val viewModel = remember(meetingId) {
            println("[MeetingScreen] Getting ViewModel from Koin...")
            object : KoinComponent {}.getKoin().get<MeetingViewModel> {
                parametersOf(meetingId, teamCardId)  // ← ОБА ПАРАМЕТРА
            }
        }

        println("[MeetingScreen] ViewModel created successfully")

        val meeting = viewModel.meeting

        var isEditing by remember { mutableStateOf(false) }

        if (viewModel.isLoading && meeting == null) {
            println("[MeetingScreen] Loading state...")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TrackMePurple)
            }
            return
        }

        meeting?.let { data ->
            println("[MeetingScreen] Rendering meeting: ${data.number}")

            // Маппим оригинальные данные API для UI
            val teamStatusUi = when (data.teamStatus) {
                "WITH_ISSUES" -> "Есть проблемы"
                "MANY_ISSUES" -> "Есть большие проблемы"
                else -> "Всё ок"
            }
            val meetingStatusUi = when (data.status) {
                "COMPLETED" -> "Состоялась"
                "COMPLETED_AS_NOT_HAPPENED" -> "Не состоялась"
                else -> "Не указана"
            }
            val displayDate = try {
                val instant = Instant.parse(data.startDate)
                val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                "${date.dayOfMonth.toString().padStart(2, '0')}.${date.monthNumber.toString().padStart(2, '0')}"
            } catch (e: Exception) {
                "01.01"
            }

            if (isEditing) {
                MeetingEditView(
                    initialData = data,
                    initialDisplayDate = displayDate,
                    initialTeamStatusUi = teamStatusUi,
                    onSave = { tasksNext, tasksCurrent, teamStatus, link, dateUi, pic ->
                        viewModel.updateMeeting(
                            meetingId = data.id,
                            tasksNext = tasksNext,
                            tasksCurrent = tasksCurrent,
                            teamStatusUI = teamStatus,
                            link = link,
                            uiDate = dateUi,
                            meetingStatusUI = meetingStatusUi
                        )
                        isEditing = false
                    },
                    onBack = { isEditing = false }
                )
            } else {
                MeetingDetailView(
                    data = data,
                    displayDate = displayDate,
                    teamStatusUi = teamStatusUi,
                    meetingStatusUi = meetingStatusUi,
                    onEditClick = { isEditing = true },
                    onBack = { navigator.pop() },
                    onResultChange = { newResUi ->
                        viewModel.updateMeeting(
                            meetingId = data.id,
                            tasksNext = data.tasksNextMeeting,
                            tasksCurrent = data.tasksCurrentMeeting,
                            teamStatusUI = teamStatusUi,
                            link = data.link,
                            uiDate = displayDate,
                            meetingStatusUI = newResUi
                        )
                    }
                )
            }
        } ?: run {
            println("[MeetingScreen] ERROR: Meeting is null! errorMessage=${viewModel.errorMessage}")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Ошибка загрузки встречи",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        viewModel.errorMessage ?: "ID: $meetingId не найден",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navigator.pop() }) {
                        Text("Назад")
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingDetailView(
    data: Meeting,
    displayDate: String,
    teamStatusUi: String,
    meetingStatusUi: String,
    onEditClick: () -> Unit,
    onBack: () -> Unit,
    onResultChange: (String) -> Unit
) {
    // Безопасная проверка, наступила ли дата встречи
    val isDatePassed = remember(data.startDate) {
        try {
            kotlin.time.Instant.parse(data.startDate) < Clock.System.now()
        } catch (e: Exception) { false }
    }

// ИСПРАВЛЕНИЕ: Проверяем что ВСЕ обязательные поля заполнены
    val allFieldsFilled = data.tasksNextMeeting.isNotBlank() &&
            data.tasksCurrentMeeting.isNotBlank() &&
            data.link.isNotBlank()

    Scaffold(topBar = { MainTopHeader() }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Встреча №${data.number}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TrackMePurple, modifier = Modifier.weight(1f))
                IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = TrackMePurple) }
            }

            // Блок кнопок результата
            // Блок кнопок результата
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isDoneSelected = meetingStatusUi == "Состоялась"
                val isFailedSelected = meetingStatusUi == "Не состоялась"

                // Показываем обе кнопки только если статус не выбран
                val showBothButtons = meetingStatusUi == "Не указана"

                if (showBothButtons || isDoneSelected) {
                    Button(
                        onClick = { onResultChange("Состоялась") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = isDatePassed && allFieldsFilled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDoneSelected) Color(0xFF6DB371) else Color(0xFF6DB371).copy(alpha = 0.12f),
                            contentColor = if (isDoneSelected) Color.White else Color(0xFF4A7A4D),
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.3f),
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Состоялась", fontWeight = if (isDoneSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                if (showBothButtons || isFailedSelected) {
                    Button(
                        onClick = { onResultChange("Не состоялась") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = isDatePassed && allFieldsFilled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFailedSelected) Color(0xFFD36D6D) else Color(0xFFD36D6D).copy(alpha = 0.12f),
                            contentColor = if (isFailedSelected) Color.White else Color(0xFF8B4545),
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.3f),
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Не состоялась", fontWeight = if (isFailedSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            // Подсказка если дата не прошла или поля не заполнены
            if (!isDatePassed || !allFieldsFilled) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = Color(0xFFFFF3CD),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                !allFieldsFilled -> "Заполните все поля встречи"
                                !isDatePassed -> "Результат можно отметить после даты встречи"
                                else -> ""
                            },
                            color = Color(0xFF856404),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            MeetingDetailField("Задачи к следующей встрече:", data.tasksNextMeeting)
            MeetingDetailField("Информация о команде (текущие задачи):", data.tasksCurrentMeeting)

            Text("Текущий статус команды:", fontWeight = FontWeight.Medium, color = Color.Black)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp).height(48.dp),
                shape = RoundedCornerShape(50),
                color = when (teamStatusUi) {
                    "Всё ок" -> Color(0xFF6DB371)
                    "Есть проблемы" -> Color(0xFFE5D170)
                    "Есть большие проблемы" -> Color(0xFFD36D6D)
                    else -> TrackMePurple
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(teamStatusUi, color = if (teamStatusUi == "Есть проблемы") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            }

            ScreenshotPickerBlock(screenshotUri = data.imageUrl, isEditing = false, onUploadClick = {})

            Spacer(modifier = Modifier.height(20.dp))

            Text("Ссылка на видеовстречу:", fontWeight = FontWeight.Medium, color = Color.Black)
            Row(modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)) {
                Surface(modifier = Modifier.weight(1f).height(56.dp).border(1.dp, TrackMePurple, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(data.link.ifEmpty { "https://..." }, color = Color.Black, maxLines = 1)
                    }
                }
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = {}, modifier = Modifier.size(56.dp).background(Color(0xFFD1F3E0), RoundedCornerShape(12.dp))) {
                    Icon(Icons.Default.Videocam, null, tint = Color(0xFF6DB371))
                }
            }

            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth().height(72.dp).padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A64EB)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Редактировать", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun MeetingDetailField(label: String, text: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(label, fontWeight = FontWeight.Medium, color = Color.Black)
        Surface(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).border(1.dp, TrackMePurple, RoundedCornerShape(25.dp)), shape = RoundedCornerShape(25.dp), color = Color.White) {
            Text(text.ifEmpty { "Не указано" }, modifier = Modifier.padding(16.dp), color = Color.Black, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingEditView(
    initialData: Meeting,
    initialDisplayDate: String,
    initialTeamStatusUi: String,
    onSave: (String, String, String, String, String, String?) -> Unit,
    onBack: () -> Unit
) {
    val imagePicker = rememberImagePicker { imageBytes ->
        // TODO: Загрузить изображение на сервер
        println("[MeetingEdit] Image selected, size: ${imageBytes.size} bytes")
        var screenshotUri = "uploaded" // Временно
    }

    var tasksNext by remember { mutableStateOf(initialData.tasksNextMeeting) }
    var tasksCurrent by remember { mutableStateOf(initialData.tasksCurrentMeeting) }
    var teamStatusUi by remember { mutableStateOf(initialTeamStatusUi) }
    var link by remember { mutableStateOf(initialData.link) }
    var selectedDateText by remember { mutableStateOf(initialDisplayDate) }
    var screenshotUri by remember { mutableStateOf(initialData.imageUrl) }
    var isExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                        val day = localDate.dayOfMonth.toString().padStart(2, '0')
                        val month = localDate.monthNumber.toString().padStart(2, '0')
                        selectedDateText = "$day.$month"
                    }
                    showDatePicker = false
                }) { Text("OK", color = TrackMePurple) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(topBar = { MainTopHeader() }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TrackMePurple) }
                Text("Редактирование", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TrackMePurple)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Дата: ", color = Color.Black)
                Surface(onClick = { showDatePicker = true }, color = TrackMePurple, shape = RoundedCornerShape(50)) {
                    Text(selectedDateText, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }

            MeetingInputRow("Задачи к следующей встрече:", tasksNext, { tasksNext = it }, isEnabled = true, isMultiline = true)
            MeetingInputRow("Информация о команде (текущие задачи):", tasksCurrent, { tasksCurrent = it }, isEnabled = true, isMultiline = true)

            Text("Текущий статус команды:", color = Color.Black, modifier = Modifier.padding(top = 16.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).border(2.dp, TrackMePurple, RoundedCornerShape(25.dp)).clip(RoundedCornerShape(25.dp)).background(Color.White)) {
                val headerBg = if (isExpanded) Color.White else when (teamStatusUi) {
                    "Всё ок" -> Color(0xFF6DB371)
                    "Есть проблемы" -> Color(0xFFE5D170)
                    "Есть большие проблемы" -> Color(0xFFD36D6D)
                    else -> TrackMePurple
                }
                Row(modifier = Modifier.fillMaxWidth().height(48.dp).background(headerBg).clickable { isExpanded = !isExpanded }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = if (isExpanded) "Выберите статус..." else teamStatusUi,
                        color = if (isExpanded || teamStatusUi == "Есть проблемы") Color.Black else Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = if (isExpanded) TrackMePurple else Color.White)
                }
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        StatusMenuItem("Всё ок", Color(0xFF6DB371)) { teamStatusUi = "Всё ок"; isExpanded = false }
                        StatusMenuItem("Есть проблемы", Color(0xFFE5D170)) { teamStatusUi = "Есть проблемы"; isExpanded = false }
                        StatusMenuItem("Есть большие проблемы", Color(0xFFD36D6D)) { teamStatusUi = "Есть большие проблемы"; isExpanded = false }
                    }
                }
            }

            ScreenshotPickerBlock(screenshotUri = screenshotUri, isEditing = true, onUploadClick = { imagePicker() })

            MeetingInputRow("Ссылка на видеовстречу:", link, { link = it }, isEnabled = true)

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    onSave(tasksNext, tasksCurrent, teamStatusUi, link, selectedDateText, screenshotUri)
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Сохранить изменения", color = Color.White, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}