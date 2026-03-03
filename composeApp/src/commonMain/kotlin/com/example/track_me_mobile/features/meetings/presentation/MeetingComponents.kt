package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.TrackMePurple

@Composable
fun MeetingInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean = true
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            enabled = isEnabled,
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TrackMePurple,
                unfocusedBorderColor = TrackMePurple
            )
        )
    }
}

@Composable
fun StatusMenuItem(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (text == "Есть проблемы") Color.Black else Color.White)
    }
}

// Добавь это в конец файла MeetingComponents.kt
@Composable
fun ScreenshotPickerBlock(
    screenshotUri: String?,
    isEditing: Boolean,
    onUploadClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = "Скриншот встречи:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Чуть увеличил высоту самого окна для баланса
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0xFFE0E0E0)) // Серое окно
                .then(if (isEditing) Modifier.clickable { onUploadClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (screenshotUri == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isEditing) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = TrackMePurple,
                            modifier = Modifier.size(100.dp) // ВОТ ТУТ СДЕЛАЛ БОЛЬШЕ (было 40, стало 100)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Нажмите, чтобы загрузить",
                            color = TrackMePurple,
                            fontSize = 15.sp, // Немного увеличил текст
                            fontWeight = FontWeight.Normal
                        )
                    } else {
                        Text("Скриншот не загружен", color = Color.Gray)
                    }
                }
            } else {
                Text("Скриншот прикреплен", color = Color(0xFF6DB371), fontWeight = FontWeight.Bold)
            }
        }
    }
}