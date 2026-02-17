package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.features.profile.presentation.components.ProfileTopHeader

@Composable
fun MeetingDetailScreen(
    tasks: String,
    status: String,
    link: String,
    date: String,
    onEditClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = { ProfileTopHeader() }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                // Этот модификатор позволяет прокручивать экран, если текста очень много
                .verticalScroll(rememberScrollState())
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = TrackMePurple)
            }

            Text("Встреча №1", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TrackMePurple)

            // Блок Дата
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                Text("Дата: ", color = Color.Gray, fontSize = 14.sp)
                Surface(color = TrackMePurple, shape = RoundedCornerShape(50)) {
                    Text(date, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp), fontSize = 12.sp)
                }
            }

            // ПОЛЕ ЗАДАЧ (Теперь без ограничения по высоте)
            Text("Задачи к следующей встрече:", color = Color.Gray, fontSize = 13.sp)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(15.dp)),
                shape = RoundedCornerShape(15.dp),
                color = Color.White
            ) {
                // Text сам расширяется под количество строк, если не задан maxLines
                Text(
                    text = if (tasks.isEmpty()) "Задач пока нет" else tasks,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(16.dp), // Внутренние отступы, чтобы текст не лип к рамке
                    lineHeight = 20.sp
                )
            }

            Text("Текущий статус команды:", color = Color.Gray, fontSize = 13.sp)
            Surface(
                modifier = Modifier.fillMaxWidth().height(46.dp).padding(top = 8.dp),
                shape = RoundedCornerShape(50),
                color = when (status) {
                    "Всё ок" -> Color(0xFF6DB371)
                    "Есть проблемы" -> Color(0xFFE5D170)
                    "Есть большие проблемы" -> Color(0xFFD36D6D)
                    else -> TrackMePurple
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(status, color = if (status == "Есть проблемы") Color.Black else Color.White, fontWeight = FontWeight.Medium)
                }
            }

            Text("Скриншот встречи:", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(top = 8.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Иконка загрузки/просмотра как на макете
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
            }

            // ПОЛЕ С ССЫЛКОЙ (В отдельной рамке, как в редакторе)
            Text("Ссылка на материалы:", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(15.dp))
                    .clickable { /* Логика открытия ссылки */ },
                shape = RoundedCornerShape(15.dp),
                color = Color.White
            ) {
                Text(
                    text = if (link.isEmpty()) "Ссылка отсутствует" else link,
                    color = if (link.isEmpty()) Color.Gray else TrackMePurple,
                    textDecoration = if (link.isEmpty()) TextDecoration.None else TextDecoration.Underline,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            TextButton(
                onClick = onEditClick,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 32.dp)
            ) {
                Text("Редактировать", color = TrackMePurple, textDecoration = TextDecoration.Underline)
            }
        }
    }
}