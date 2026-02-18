package com.example.track_me_mobile.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.core.ui.theme.*


@Composable
@Preview
fun ProfileScreen() {
    var isGlobalEditMode by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("Иванов Иван Иванович") }
    var email by remember { mutableStateOf("ivan@mail.ru") }
    var phone by remember { mutableStateOf("+7 999 999-99-99") }
    var telegram by remember { mutableStateOf("@IVANIVAN") }

    Scaffold(
        topBar = { MainTopHeader() },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 34.dp, vertical = 35.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Личный кабинет",
                fontSize = 32.sp,
                color = TrackMePurple,
                modifier = Modifier.padding(bottom = 35.dp)
            )

            // --- АВАТАР ---
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(TrackMePurpleLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Аватар",
                    modifier = Modifier.size(120.dp),
                    tint = TrackMePurple
                )

                // Кнопка выбора картинки (стрелочка)
                if (isGlobalEditMode) {
                    IconButton(
                        onClick = {
                            /* Для выбора из галереи в KMP (commonMain)
                               нужно использовать библиотеку Peekaboo или FilePicker.
                               Ниже я напишу, как это сделать.
                            */
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Загрузить",
                            tint = TrackMePurple,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
            Text("Администратор", color = TrackMePurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(15.dp))

            // Поля данных
            ProfileEditableRow(value = name, onValueChange = { name = it }, showControls = isGlobalEditMode)
            ProfileEditableRow(value = email, onValueChange = { email = it }, showControls = isGlobalEditMode)
            ProfileEditableRow(value = phone, onValueChange = { phone = it }, showControls = isGlobalEditMode)
            ProfileEditableRow(value = telegram, onValueChange = { telegram = it }, showControls = isGlobalEditMode)

            Spacer(modifier = Modifier.height(25.dp))

            if (isGlobalEditMode) {
                // Кнопка СОХРАНИТЬ
                Button(
                    onClick = { isGlobalEditMode = false },
                    modifier = Modifier.fillMaxWidth(0.6f).height(42.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple)
                ) {
                    Text("Сохранить", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Кнопка КАРТОЧКИ КОМАНД
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(0.80f).height(42.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple)
                ) {
                    Text("Карточки команд", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ВЕРНУЛ КНОПКУ: ЗАГРУЗИТЬ ОТЧЕТ
                TextButton(onClick = { }) {
                    Text("Загрузить отчет", color = TrackMePurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // Кнопка РЕДАКТИРОВАТЬ
                TextButton(onClick = { isGlobalEditMode = true }) {
                    Text("Редактировать", color = TrackMePurple, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileEditableRow(value: String, onValueChange: (String) -> Unit, showControls: Boolean) {
    var isFieldActive by remember(showControls) { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.weight(1f).height(42.dp),
            color = TrackMePurpleLight.copy(alpha = 0.2f),
            shape = RoundedCornerShape(50)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (isFieldActive) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(color = TrackMePurple, fontSize = 16.sp, textAlign = TextAlign.Center),
                        singleLine = true,
                        cursorBrush = SolidColor(TrackMePurple),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                    )
                } else {
                    Text(text = value, color = TrackMePurple, fontSize = 16.sp)
                }
            }
        }
        if (showControls) {
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = { isFieldActive = !isFieldActive }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Edit, null, tint = if (isFieldActive) Color.Gray else TrackMePurple)
            }
        }
    }
}