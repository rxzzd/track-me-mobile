package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
// floatingActionButton = { FeedbackFab() }, floatingActionButtonPosition = FabPosition.End
// Вызов обратной связи в добавлять в Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDialog(
    onDismiss: () -> Unit
) {
    var dropdownExpanded1 by remember { mutableStateOf(false) }
    var dropdownExpanded2 by remember { mutableStateOf(false) }
    var selectedRating1 by remember { mutableStateOf("") }
    var selectedRating2 by remember { mutableStateOf("") }

    var bugCount by remember { mutableStateOf("0") }
    var liked by remember { mutableStateOf("") }
    var disliked by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {

                // Заголовок + кнопка закрытия
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Помогите нам стать лучше",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B2FBE),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "✕",
                        fontSize = 16.sp,
                        color = Color(0xFF9B59B6),
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(start = 8.dp)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                // 1. Оценка дизайна
                Text(
                    text = "1. Оцените дизайн сервиса (1 — очень плохо, 5 — отлично):",
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded1,
                    onExpandedChange = { dropdownExpanded1 = it }
                ) {
                    OutlinedTextField(
                        value = if (selectedRating1.isEmpty()) "Выберите оценку" else selectedRating1,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (dropdownExpanded1) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF7B2FBE)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFCCCCCC),
                            focusedBorderColor = Color(0xFF7B2FBE),
                            unfocusedTextColor = if (selectedRating1.isEmpty()) Color.Gray else Color.Black,
                            focusedTextColor = Color.Black
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded1,
                        onDismissRequest = { dropdownExpanded1 = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        (1..5).forEach { rating ->
                            DropdownMenuItem(
                                text = { Text(text = rating.toString(), color = Color.Black) },
                                onClick = {
                                    selectedRating1 = rating.toString()
                                    dropdownExpanded1 = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Оценка удобства
                Text(
                    text = "2. Оцените удобство использования (1 — очень плохо, 5 — отлично):",
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded2,
                    onExpandedChange = { dropdownExpanded2 = it }
                ) {
                    OutlinedTextField(
                        value = if (selectedRating2.isEmpty()) "Выберите оценку" else selectedRating2,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (dropdownExpanded2) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF7B2FBE)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFCCCCCC),
                            focusedBorderColor = Color(0xFF7B2FBE),
                            unfocusedTextColor = if (selectedRating2.isEmpty()) Color.Gray else Color.Black,
                            focusedTextColor = Color.Black
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded2,
                        onDismissRequest = { dropdownExpanded2 = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        (1..5).forEach { rating ->
                            DropdownMenuItem(
                                text = { Text(text = rating.toString(), color = Color.Black) },
                                onClick = {
                                    selectedRating2 = rating.toString()
                                    dropdownExpanded2 = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Количество багов
                Text(
                    text = "3. Укажите количество багов, с которыми вы столкнулись: ",
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bugCount,
                    onValueChange = { if (it.all { c -> c.isDigit() }) bugCount = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedBorderColor = Color(0xFF7B2FBE)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Что понравилось
                Text(text = "4. Что вам понравилось больше всего:", fontSize = 13.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = liked,
                    onValueChange = { liked = it },
                    placeholder = { Text("Опишите, что вам понравилось...", color = Color.LightGray, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedBorderColor = Color(0xFF7B2FBE)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Что вам не понравилось
                Text(text = "5. Что вам понравилось меньше всего:", fontSize = 13.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = disliked,
                    onValueChange = { disliked = it },
                    placeholder = { Text("Опишите, что можно улучшить...", color = Color.LightGray, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedBorderColor = Color(0xFF7B2FBE)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Рекомендации
                Text(text = "6. Ваши рекомендации по улучшению:", fontSize = 13.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = suggestions,
                    onValueChange = { suggestions = it },
                    placeholder = { Text("Ваши идеи по улучшению сервиса...", color = Color.LightGray, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedBorderColor = Color(0xFF7B2FBE)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Кнопка отправки
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B2FC9))
                ) {
                    Text(text = "Отправить отзыв", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FeedbackFab(modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    ExtendedFloatingActionButton(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(50.dp),
        containerColor = Color(0xFF8B2FC9),
        contentColor = Color.White,
        modifier = modifier
    ) {
        Text(
            text = "Обратная связь",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }

    if (showDialog) {
        ContactDialog(onDismiss = { showDialog = false })
    }
}

@Preview(showBackground = true)
@Composable
fun ContactDialogPreview() {
    MaterialTheme {
        ContactDialog(onDismiss = {})
    }
}