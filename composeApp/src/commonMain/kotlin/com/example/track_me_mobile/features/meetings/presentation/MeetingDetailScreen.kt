package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.features.profile.presentation.components.ProfileTopHeader
import com.example.track_me_mobile.features.meetings.presentation.components.*

@Composable
fun MeetingDetailScreen(
    tasks: String,
    teamInfo: String,
    status: String,
    linkRecord: String,
    linkVideo: String,
    date: String,
    meetingResult: String,
    screenshotUri: String?, // Добавлен 8-й параметр
    onEditClick: () -> Unit,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(topBar = { ProfileTopHeader() }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Заголовок
            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Встреча 1",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrackMePurple,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, null, tint = TrackMePurple)
                }
            }

            // Дата и статус (Состоялась/Нет)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Дата: ", color = Color.Black, fontSize = 16.sp)
                Surface(color = TrackMePurple, shape = RoundedCornerShape(50)) {
                    Text(
                        date,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                if (meetingResult.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = if (meetingResult == "Состоялась") Color(0xFF6DB371) else Color(0xFFD36D6D),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            meetingResult,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Поля задач и информации
            MeetingDetailField("Задачи к следующей встрече:", tasks)
            MeetingDetailField("Информация о команде:", teamInfo)

            // Текущий статус команды
            Text("Текущий статус команды:", fontWeight = FontWeight.Medium, color = Color.Black)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                color = when (status) {
                    "Всё ок" -> Color(0xFF6DB371)
                    "Есть проблемы" -> Color(0xFFE5D170)
                    else -> Color(0xFFD36D6D)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        status,
                        color = if (status == "Есть проблемы") Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // СКРИНШОТ СРАЗУ ПОСЛЕ СТАТУСА (как в EditScreen)
            ScreenshotPickerBlock(
                screenshotUri = screenshotUri,
                isEditing = false,
                onUploadClick = {}
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Запись встречи
            MeetingDetailField("Запись встречи:", linkRecord.ifEmpty { "Ссылка отсутствует" })

            // Видеовстреча
            Text("Ссылка на видеовстречу:", fontWeight = FontWeight.Medium, color = Color.Black)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(1.dp, TrackMePurple, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(linkVideo.ifEmpty { "https://..." }, color = Color.Black, maxLines = 1)
                    }
                }
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        if (linkVideo.isNotEmpty()) {
                            val url = if (!linkVideo.startsWith("http")) "https://$linkVideo" else linkVideo
                            try {
                                uriHandler.openUri(url)
                            } catch (e: Exception) {
                                // Ошибка открытия ссылки
                            }
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFD1F3E0), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Default.Videocam,
                        null,
                        tint = Color(0xFF6DB371),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Кнопка редактирования
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A64EB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Редактировать", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MeetingDetailField(label: String, text: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(label, fontWeight = FontWeight.Medium, color = Color.Black)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .border(1.dp, TrackMePurple, RoundedCornerShape(25.dp)),
            shape = RoundedCornerShape(25.dp),
            color = Color.White
        ) {
            Text(
                text,
                modifier = Modifier.padding(16.dp),
                color = Color.Black,
                fontSize = 14.sp
            )
        }
    }
}