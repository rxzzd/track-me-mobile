package com.example.track_me_mobile.features.streams.presentation.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

import org.jetbrains.compose.resources.Font
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.Mulish_SemiBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit,
) {
    val mulishFamily = FontFamily(Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold))
    var showDatePicker by remember { mutableStateOf(false) }

    // Состояние календаря
    val datePickerState = rememberDatePickerState()

    Box(
        modifier = modifier
            .width(120.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(color = Color(0xFFE6D7FB))
            .clickable { showDatePicker = true }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = value.ifEmpty { "__.__.____" },
            style = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF44069A),
                fontFamily = mulishFamily,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formattedDate = formatMillisToDate(millis)
                        onValueChange(formattedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("ОК", color = Color(0xFF8338EB))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена", color = Color(0xFF8338EB))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF8338EB),
                    todayDateBorderColor = Color(0xFF8338EB),
                    todayContentColor = Color(0xFF8338EB)
                )
            )
        }
    }
}

private fun formatMillisToDate(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val dateTime = instant.toLocalDateTime(TimeZone.UTC)

    val year = dateTime.year
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')

    return "$year-$month-$day"
}