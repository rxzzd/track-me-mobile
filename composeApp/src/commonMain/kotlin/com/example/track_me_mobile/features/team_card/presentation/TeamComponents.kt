package com.example.track_me_mobile.features.team_card.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*


data class TeamFilterData(
    val stream: String = "",
    val markets: List<String> = emptyList(),
    val trl: String = "",
    val description: String = "",
    val trackerName: String = ""
)

data class MeetingData(
    val date: String,
    val title: String
)

@Composable
fun TrackerRow(
    name: String = "",
    onNameChanged: ((String) -> Unit)? = null
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(name) { mutableStateOf(name) }
    val focusRequester = remember { FocusRequester() }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Трекер:", fontSize = 14.sp, color = TextBlack)
        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .background(TrackMePurpleLight.copy(alpha = 0.3f), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isEditing && onNameChanged != null) {
                BasicTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    textStyle = TextStyle(color = TrackMePurple, fontSize = 14.sp),
                    cursorBrush = SolidColor(TrackMePurple),
                    singleLine = true,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .widthIn(min = 80.dp),
                    decorationBox = { innerTextField ->
                        if (editText.isEmpty()) {
                            Text(
                                text = "Введите имя трекера",
                                color = TrackMePurple.copy(alpha = 0.4f),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            } else {
                Text(
                    text = name.ifEmpty { "Введите имя трекера" },
                    color = if (name.isEmpty()) TrackMePurple.copy(alpha = 0.4f) else TrackMePurple,
                    fontSize = 14.sp
                )
            }
        }

        if (onNameChanged != null) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (isEditing) {
                        onNameChanged(editText)
                        isEditing = false
                    } else {
                        isEditing = true
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Сохранить" else "Редактировать",
                    tint = TrackMePurple,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PurpleChip(text: String) {
    Box(
        modifier = Modifier
            .background(TrackMePurple, RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoTextRow(label: String, value: String, isPurple: Boolean = false) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 14.sp, color = TextBlack)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = if (isPurple) TrackMePurple else TextBlack,
            fontWeight = if (isPurple) FontWeight.Bold else FontWeight.Normal
        )
    }
}
