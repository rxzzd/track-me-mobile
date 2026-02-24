package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MeetingsCountDropdown(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(5, 10, 15, 20, 25, 30)

    val purpleColor = Color(0xFF8338EB)
    val lightPurple = Color(0xFFE8D5FF)

    Box(modifier = modifier) {
        // Кнопка выбора
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(40.dp)
                .background(lightPurple.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .border(2.dp, purpleColor, RoundedCornerShape(20.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value == 0) "Выберите количество" else value.toString(),
                    color = purpleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = purpleColor
                )
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(220.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {
            options.forEach { count ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = count.toString(),
                            color = purpleColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    onClick = {
                        onValueChange(count)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (count == value) lightPurple.copy(alpha = 0.2f) else Color.Transparent
                    )
                )
            }

            Divider(color = purpleColor.copy(alpha = 0.2f))

            DropdownMenuItem(
                text = {
                    Text(
                        text = "Своё значение",
                        color = purpleColor,
                        fontWeight = FontWeight.Normal
                    )
                },
                onClick = {
                    // TODO: Открыть диалог для ввода кастомного значения
                    expanded = false
                }
            )
        }
    }
}