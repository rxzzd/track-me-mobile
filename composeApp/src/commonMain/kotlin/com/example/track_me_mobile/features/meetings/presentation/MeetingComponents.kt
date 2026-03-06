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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun MeetingInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean = true,
    isMultiline: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            enabled = isEnabled,
            singleLine = !isMultiline,
            minLines = if (isMultiline) 3 else 1,
            maxLines = if (isMultiline) 5 else 1,
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TrackMePurple,
                unfocusedBorderColor = TrackMePurple,
                disabledBorderColor = TrackMePurple.copy(alpha = 0.5f)
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
                .height(200.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0xFFE0E0E0))
                .then(
                    if (isEditing) Modifier.clickable { onUploadClick() }
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            // ИСПРАВЛЕННАЯ ПРОВЕРКА: проверяем что URL не содержит "null" и не пустой
            val hasImage = !screenshotUri.isNullOrEmpty() &&
                    !screenshotUri.contains("/null") &&
                    screenshotUri.length > 50

            if (hasImage) {
                // TODO: Отображаем реальное изображение
                AsyncImage(
                    model = screenshotUri,
                    contentDescription = "Скриншот встречи",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                // Скриншота нет - показываем placeholder
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isEditing) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = TrackMePurple,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Нажмите, чтобы загрузить",
                            color = TrackMePurple,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Прикрепить скриншот",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}