package com.example.track_me_mobile.features.team_card.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.track_me_mobile.core.ui.theme.*
import kotlinx.datetime.toLocalDateTime
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.features.teams.presentation.DarkPurple
import com.example.track_me_mobile.features.teams.presentation.PrimaryPurple
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.arrowback
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMeetingsScreen(
    viewModel: TeamMeetingsViewModel,
    onBackClick: () -> Unit,
    onMeetingClick: (String, String) -> Unit
) {
    val navigator = LocalNavigator.currentOrThrow
    val montserrat = MontserratFontFamily()

    var showPlanDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Показываем стрелку только для ADMIN и SUPER_ADMIN

                Icon(
                    painter = painterResource(Res.drawable.arrowback),
                    contentDescription = null,
                    tint = DarkPurple,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navigator.pop() }
                )
                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Встречи",
                    fontFamily = montserrat,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkPurple,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Контент в зависимости от состояния
            when (val s = viewModel.state) {
                is TeamMeetingsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TrackMePurple)
                    }
                }
                is TeamMeetingsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(s.message, color = ErrorRed, fontSize = 16.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadMeetings() },
                                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple)
                            ) {
                                Text("Повторить")
                            }
                        }
                    }
                }
                is TeamMeetingsState.Success -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(1.dp, TrackMePurple, RoundedCornerShape(28.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                if (s.meetings.isEmpty()) {
                                    Text(
                                        "У данной команды еще не было встреч",
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(top = 40.dp)
                                    )
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(s.meetings) { meeting ->
                                            MeetingItemRowWithMenu(
                                                meeting = meeting,
                                                onMeetingClick = {
                                                    println("[TeamMeetingsScreen] Click: id=${meeting.id}, teamId=${meeting.teamCardId}")
                                                    onMeetingClick(meeting.id, meeting.teamCardId)
                                                },
                                                onDateChange = { newDateMillis ->
                                                    viewModel.updateMeetingDate(meeting, newDateMillis) {
                                                        println("[TeamMeetingsScreen] Date updated for: ${meeting.id}")
                                                    }
                                                },
                                                onDelete = {
                                                    viewModel.deleteMeeting(meeting.id) {
                                                        println("[TeamMeetingsScreen] Meeting deleted: ${meeting.id}")
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showPlanDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(48.dp),
                                enabled = !viewModel.isCreating
                            ) {
                                if (viewModel.isCreating) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text("Запланировать", color = Color.White, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("Выбрать", color = TrackMePurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена", color = TrackMePurple)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Plan Meeting Dialog
    // Plan Meeting Dialog
    if (showPlanDialog) {
        val currentMeetings = (viewModel.state as? TeamMeetingsState.Success)?.meetings ?: emptyList()
        val maxNumber = currentMeetings
            .mapNotNull { it.number.toIntOrNull() }
            .maxOrNull() ?: 0
        val nextNumber = maxNumber + 1

        // ДОБАВИТЬ: проверка что дата не в прошлом
        var dateError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPlanDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            content = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, TrackMePurple, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Запланировать встречу #$nextNumber",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            IconButton(onClick = {
                                showPlanDialog = false
                                dateError = null
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Закрыть",
                                    tint = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Дата и время:",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = {
                                    showDatePicker = true
                                    dateError = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = selectedDateMillis?.let { millis ->
                                        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
                                        val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                                        val date = localDateTime.date
                                        "${date.dayOfMonth.toString().padStart(2, '0')}.${date.monthNumber.toString().padStart(2, '0')}.${date.year}"
                                    } ?: "Выбрать",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // ДОБАВИТЬ: отображение ошибки валидации
                        if (dateError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dateError!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    selectedDateMillis?.let { millis ->
                                        val selectedInstant = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
                                        val isoDate = selectedInstant.toString()

                                        viewModel.planNewMeeting(isoDate) {
                                            showPlanDialog = false
                                            selectedDateMillis = null
                                            dateError = null
                                        }
                                    }
                                },
                                enabled = selectedDateMillis != null && !viewModel.isCreating,
                                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(48.dp)
                            ) {
                                if (viewModel.isCreating) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        "Создать",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MeetingItemRowWithMenu(
    meeting: MeetingItemUI,
    onMeetingClick: () -> Unit,
    onDateChange: (Long) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val backgroundColor = when (meeting.status) {
        "COMPLETED" -> Color(0xFF6DB371)
        "COMPLETED_AS_NOT_HAPPENED" -> Color(0xFFD36D6D)
        "SCHEDULED" -> TrackMePurple
        else -> Color.Gray
    }

    val textColor = Color.White

    val isCompleted = meeting.status == "COMPLETED" || meeting.status == "COMPLETED_AS_NOT_HAPPENED"

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onMeetingClick,
                    onLongClick = {
                        if (!isCompleted) {
                            showMenu = true
                        }
                    }
                )
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = meeting.date,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            VerticalDivider(
                modifier = Modifier.height(20.dp),
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = meeting.title,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Popup меню
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Изменить дату") },
                onClick = {
                    showMenu = false
                    showDatePickerDialog = true
                },
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("Удалить", color = Color.Red) },
                onClick = {
                    showMenu = false
                    showDeleteConfirmDialog = true
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            )
        }
    }

    // DatePicker для изменения даты
    // DatePicker для изменения даты
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateChange(millis)
                        }
                        showDatePickerDialog = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("Изменить", color = TrackMePurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        "Изменить дату встречи",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Удалить встречу?") },
            text = { Text("Вы уверены, что хотите удалить ${meeting.title}? Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text("Удалить", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

}


@Composable
fun MeetingItemRow(meeting: MeetingItemUI, onClick: () -> Unit) {
    // Определяем цвет фона всего контейнера в зависимости от статуса
    val backgroundColor = when (meeting.status) {
        "COMPLETED" -> Color(0xFF6DB371)      // Зеленый - состоялась
        "COMPLETED_AS_NOT_HAPPENED" -> Color(0xFFD36D6D)      // Красный - не состоялась
        "SCHEDULED" -> TrackMePurple          // Фиолетовый - запланирована
        else -> Color.Gray                     // Серый - неизвестный статус
    }

    // Цвет текста (белый на цветном фоне)
    val textColor = Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp))  // Скругление углов
            .background(backgroundColor)       // Заливка цветом
            .padding(16.dp),                   // Внутренние отступы
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = meeting.date,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        VerticalDivider(
            modifier = Modifier.height(20.dp),
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.5f)  // Полупрозрачный разделитель
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = meeting.title,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}