package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCreateScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("TrackMe", color = Color.White, fontWeight = FontWeight.Bold)
                },
                // Добавляем иконку меню справа
                actions = {
                    IconButton(onClick = { /* Открыть меню */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Меню",
                            tint = Color.White
                        )
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
            // Кнопка назад
            Text(
                text = "←",
                color = TrackMePurple,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clickable { onBackClick() }
            )

            // --- Поле Трекер с иконкой редактирования ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Трекер:",
                    fontSize = 14.sp,
                    color = TextBlack
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .background(TrackMePurpleLight.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Иванов Иван Иванович", color = TrackMePurple, fontSize = 14.sp)
                }

                // Добавляем иконку карандаша в конец строки
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* Редактировать */ }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = TrackMePurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Секции с кнопкой добавить (+) ---
            // Кнопки на фиксированном расстоянии 16.dp от текста
            AddSectionRow(label = "Поток:")
            Spacer(modifier = Modifier.height(16.dp))
            AddSectionRow(label = "Рынки НТИ:")
            Spacer(modifier = Modifier.height(16.dp))
            AddSectionRow(label = "TRL:")

            Spacer(modifier = Modifier.height(24.dp))

            // Поле описания
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.dp, TrackMePurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("/", color = TextGray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка Создать
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { /* Действие */ },
                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                    modifier = Modifier
                        .width(200.dp)
                        .height(45.dp),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text(
                        text = "Создать",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AddSectionRow(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = TextBlack)
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(TrackMePurple)
                .clickable { /* Добавить */ },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview
@Composable
fun TeamCreateScreenPreview() {
    MaterialTheme {
        Surface(color = BackgroundWhite) {
            TeamCreateScreen()
        }
    }
}