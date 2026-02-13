package com.example.track_me_mobile.features.teams.presentation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*
import androidx.compose.material3.MaterialTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamInfoScreen(
    data: TeamFilterData, // Данные приходят снаружи (из БД или с экрана создания)
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("←", color = TrackMePurple, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBackClick() })
            Spacer(modifier = Modifier.height(16.dp))

            // Данные
            InfoTextRow(label = "Название потока:", value = data.stream.ifEmpty { "Не указано" }, isPurple = true)
            InfoTextRow(label = "Кол-во команд:", value = "12 команд", isPurple = true)
            InfoTextRow(label = "Дата проведения:", value = "01.01.2025 - 10.10.2025", isPurple = true)

            Spacer(modifier = Modifier.height(24.dp))

            TrackerRow() // Из TeamComponents.kt

            Spacer(modifier = Modifier.height(16.dp))

            // Рынки
            Text("Рынки НТИ:", fontSize = 14.sp, color = TextBlack)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                if (data.markets.isEmpty()) {
                    PurpleChip(text = "Нет рынков")
                } else {
                    data.markets.forEach { market ->
                        PurpleChip(text = market) // Из TeamComponents.kt
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TRL
            Text("TRL:", fontSize = 14.sp, color = TextBlack)
            Spacer(modifier = Modifier.height(8.dp))
            PurpleChip(text = data.trl.ifEmpty { "Не указано" })

            Spacer(modifier = Modifier.height(24.dp))

            // Описание
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TrackMePurple, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Описание карточки команд... (здесь будет текст)",
                    color = TextBlack,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onEditClick) {
                    Text("Редактировать", color = TrackMePurple, fontSize = 16.sp)
                }

                Button(
                    onClick = { /* Встречи */ },
                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(45.dp).width(150.dp)
                ) {
                    Text("Встречи", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamInfoScreenPreview() {

    val sampleData = TeamFilterData(
        stream = "Информационные технологии",
        markets = listOf("HealthNet", "TechNet", "SafeNet"),
        trl = "6-8"
    )

    MaterialTheme {
        TeamInfoScreen(
            data = sampleData,
            onBackClick = { /* Ничего не делаем в превью */ },
            onEditClick = { /* Ничего не делаем в превью */ }
        )
    }
}