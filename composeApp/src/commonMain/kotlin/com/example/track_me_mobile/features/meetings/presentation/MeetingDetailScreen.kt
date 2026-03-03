package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.features.profile.presentation.components.ProfileTopHeader
import com.example.track_me_mobile.features.meetings.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MeetingDetailScreen(
    tasks: String,
    teamInfo: String,
    status: String,
    linkRecord: String,
    linkVideo: String,
    date: String,
    meetingResult: String,
    screenshotUri: String?,
    onEditClick: () -> Unit,
    onBack: () -> Unit,
    onResultChange: (String) -> Unit
) {
    // Логика: кнопки активны, только если наступил СЛЕДУЮЩИЙ день после встречи
    val isDatePassed = remember(date) {
        try {
            val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)

            val meetingDate = sdf.parse(date)
            val meetingCalendar = Calendar.getInstance().apply {
                if (meetingDate != null) time = meetingDate
                set(Calendar.YEAR, currentYear)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            meetingCalendar.before(today)
        } catch (e: Exception) { false }
    }

    val allFieldsFilled = tasks.isNotBlank() && teamInfo.isNotBlank() &&
            linkRecord.isNotBlank() && linkVideo.isNotBlank() && screenshotUri != null

    Scaffold(topBar = { ProfileTopHeader() }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Встреча 1", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TrackMePurple, modifier = Modifier.weight(1f))
                IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = TrackMePurple) }
            }

            // Блок кнопок результата
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val isDoneSelected = meetingResult == "Состоялась"
                Button(
                    onClick = { onResultChange("Состоялась") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = isDatePassed && allFieldsFilled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDoneSelected) Color(0xFF6DB371) else Color(0xFF6DB371).copy(alpha = 0.12f),
                        contentColor = if (isDoneSelected) Color.White else Color(0xFF4A7A4D),
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(50)
                ) { Text("Состоялась", fontWeight = if (isDoneSelected) FontWeight.Bold else FontWeight.Normal) }

                val isFailedSelected = meetingResult == "Не состоялась"
                Button(
                    onClick = { onResultChange("Не состоялась") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = isDatePassed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFailedSelected) Color(0xFFD36D6D) else Color(0xFFD36D6D).copy(alpha = 0.12f),
                        contentColor = if (isFailedSelected) Color.White else Color(0xFF8B4545),
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(50)
                ) { Text("Не состоялась", fontWeight = if (isFailedSelected) FontWeight.Bold else FontWeight.Normal) }
            }

            MeetingDetailField("Задачи к следующей встрече:", tasks)
            MeetingDetailField("Информация о команде:", teamInfo)

            Text("Текущий статус команды:", fontWeight = FontWeight.Medium, color = Color.Black)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp).height(48.dp),
                shape = RoundedCornerShape(50),
                color = when (status) {
                    "Всё ок" -> Color(0xFF6DB371)
                    "Есть проблемы" -> Color(0xFFE5D170)
                    "Есть большие проблемы" -> Color(0xFFD36D6D)
                    else -> TrackMePurple // Фиолетовый для "Не указана"
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(status, color = if (status == "Есть проблемы") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            }

            ScreenshotPickerBlock(screenshotUri = screenshotUri, isEditing = false, onUploadClick = {})

            Spacer(modifier = Modifier.height(20.dp))
            MeetingDetailField("Запись встречи:", linkRecord.ifEmpty { "Ссылка отсутствует" })

            Text("Ссылка на видеовстречу:", fontWeight = FontWeight.Medium, color = Color.Black)
            Row(modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)) {
                Surface(modifier = Modifier.weight(1f).height(56.dp).border(1.dp, TrackMePurple, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), color = Color.White) {
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(linkVideo.ifEmpty { "https://..." }, color = Color.Black, maxLines = 1)
                    }
                }
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = {}, modifier = Modifier.size(56.dp).background(Color(0xFFD1F3E0), RoundedCornerShape(12.dp))) {
                    Icon(Icons.Default.Videocam, null, tint = Color(0xFF6DB371))
                }
            }

            // Кнопка редактирования: ШИРЕ, ВЫШЕ, ЖИРНЕЕ
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(bottom = 16.dp),
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
            Text(text, modifier = Modifier.padding(16.dp), color = Color.Black, fontSize = 14.sp)
        }
    }
}