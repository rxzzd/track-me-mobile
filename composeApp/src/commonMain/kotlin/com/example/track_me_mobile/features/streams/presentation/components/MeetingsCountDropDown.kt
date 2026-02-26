package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun MeetingsCountDropdown(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var customValueText by remember { mutableStateOf("") }

    val options = listOf(5, 10, 15, 20)
    val fieldBgColor = Color(0xFFE6D5FF)
    val textColor = Color(0xFF8338EB)
    val borderColor = textColor.copy(alpha = 0.2f)

    var contentHeight by remember { mutableStateOf(0) }

    // --- Диалог для ввода своего значения ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Свое значение", color = textColor, fontSize = 18.sp) },
            text = {
                Column {
                    Text("Введите количество встреч:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customValueText,
                        onValueChange = { input ->
                            // Разрешаем только цифры и длину до 2 символов
                            if (input.all { it.isDigit() } && input.length <= 2) {
                                customValueText = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = textColor,
                            unfocusedBorderColor = borderColor
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = customValueText.toIntOrNull()
                        if (parsed != null && parsed in 1..99) {
                            onValueChange(parsed)
                            showDialog = false
                            customValueText = ""
                        }
                    }
                ) {
                    Text("ОК", color = textColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена", color = Color.Gray)
                }
            }
        )
    }

    Box(modifier = modifier.width(220.dp), contentAlignment = Alignment.TopCenter) {

        if (expanded) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -contentHeight),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    modifier = Modifier
                        .width(220.dp)
                        .onGloballyPositioned { coordinates ->
                            contentHeight = coordinates.size.height
                        },
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = fieldBgColor,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "Выберите количество",
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            textAlign = TextAlign.Center,
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        options.forEach { option ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onValueChange(option)
                                        expanded = false
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.toString(),
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = if (option == value) FontWeight.ExtraBold else FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = borderColor
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expanded = false
                                    showDialog = true
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Свое значение",
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            shape = RoundedCornerShape(
                topStart = if (expanded) 0.dp else 20.dp,
                topEnd = if (expanded) 0.dp else 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            color = fieldBgColor,
            shadowElevation = if (expanded) 8.dp else 0.dp,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(24.dp))

                Text(
                    text = if (value == 0) "Выберите количество" else value.toString(),
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    style = TextStyle(
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    )
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}