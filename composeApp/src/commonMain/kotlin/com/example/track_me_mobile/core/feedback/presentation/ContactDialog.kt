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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.track_me_mobile.core.feedback.presentation.FeedbackViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDialog(
    viewModel: FeedbackViewModel,
    onDismiss: () -> Unit
) {
    // Если флаг isSubmitted во вью-модели стал true, показываем только SuccessDialog
    if (viewModel.isSubmitted) {
        SuccessDialog(
            onDismiss = {
                viewModel.resetSubmitted() // Сбрасываем состояние для следующего раза
                onDismiss() // Закрываем диалог
            }
        )
    } else {
        // Основной диалог с формой ввода
        var dropdownExpanded1 by remember { mutableStateOf(false) }
        var dropdownExpanded2 by remember { mutableStateOf(false) }

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

                    // 1. Дизайн
                    Text(text = "1. Оцените дизайн сервиса (1-5):", fontSize = 13.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded1,
                        onExpandedChange = { dropdownExpanded1 = it }
                    ) {
                        OutlinedTextField(
                            value = viewModel.designRating.ifEmpty { "Выберите оценку" },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded1) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = dropdownExpanded1, onDismissRequest = { dropdownExpanded1 = false }) {
                            (1..5).forEach { rating ->
                                DropdownMenuItem(
                                    text = { Text(rating.toString()) },
                                    onClick = {
                                        viewModel.updateDesignRating(rating.toString())
                                        dropdownExpanded1 = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Удобство
                    Text(text = "2. Оцените удобство (1-5):", fontSize = 13.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded2,
                        onExpandedChange = { dropdownExpanded2 = it }
                    ) {
                        OutlinedTextField(
                            value = viewModel.usabilityRating.ifEmpty { "Выберите оценку" },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded2) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = dropdownExpanded2, onDismissRequest = { dropdownExpanded2 = false }) {
                            (1..5).forEach { rating ->
                                DropdownMenuItem(
                                    text = { Text(rating.toString()) },
                                    onClick = {
                                        viewModel.updateUsabilityRating(rating.toString())
                                        dropdownExpanded2 = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Баги
                    Text(text = "3. Укажите количество багов:", fontSize = 13.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.bugsCount,
                        onValueChange = { viewModel.updateBugsCount(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Что понравилось
                    Text(text = "4. Что понравилось:", fontSize = 13.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.liked,
                        onValueChange = { viewModel.updateLiked(it) },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Что не понравилось
                    Text(text = "5. Что не понравилось:", fontSize = 13.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.disliked,
                        onValueChange = { viewModel.updateDisliked(it) },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. Рекомендации
                    Text(text = "6. Рекомендации:", fontSize = 13.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.suggestions,
                        onValueChange = { viewModel.updateSuggestions(it) },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )

                    // Ошибка
                    viewModel.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = error, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Кнопка отправки
                    Button(
                        onClick = { viewModel.submitFeedback { } },
                        enabled = !viewModel.isSubmitting,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B2FC9))
                    ) {
                        if (viewModel.isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = "Отправить отзыв", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Спасибо!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B2FBE)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ваш отзыв успешно отправлен. Мы ценим вашу помощь!",
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B2FC9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Отлично", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FeedbackFab(modifier: Modifier = Modifier) {
    // Внедряем зависимость через Koin
    val viewModel = koinInject<FeedbackViewModel>()
    var showDialog by remember { mutableStateOf(false) }

    ExtendedFloatingActionButton(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(50.dp),
        containerColor = Color(0xFF8B2FC9),
        contentColor = Color.White,
        modifier = modifier
    ) {
        Text(text = "Обратная связь", fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }

    if (showDialog) {
        ContactDialog(
            viewModel = viewModel,
            onDismiss = { showDialog = false }
        )
    }
}