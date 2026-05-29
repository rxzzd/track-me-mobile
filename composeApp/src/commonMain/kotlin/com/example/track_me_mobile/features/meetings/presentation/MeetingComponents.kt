package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import org.koin.compose.koinInject

fun hasValidScreenshotUri(screenshotUri: String?): Boolean =
    !screenshotUri.isNullOrEmpty() &&
            !screenshotUri.contains("/null") &&
            screenshotUri.length > 50

fun getScreenshotPlaceholderText(isEditing: Boolean): String = if (isEditing) {
    "Нажмите, чтобы загрузить"
} else {
    "Прикрепить скриншот"
}

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

        val hasImage = hasValidScreenshotUri(screenshotUri)

        var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var hasError by remember { mutableStateOf(false) }
        var showFullScreen by remember { mutableStateOf(false) }

        val httpClient = koinInject<HttpClient>()

        // Загружаем изображение
        if (hasImage && screenshotUri != null) {
            LaunchedEffect(screenshotUri) {
                try {
                    isLoading = true
                    hasError = false
                    val response: HttpResponse = httpClient.get(screenshotUri)
                    val bytes: ByteArray = response.body()
                    imageBitmap = loadImageBitmap(bytes)
                    isLoading = false
                } catch (e: Exception) {
                    println("[Screenshot] Load failed: ${e.message}")
                    hasError = true
                    isLoading = false
                }
            }
        }

        // АДАПТИВНЫЙ КОНТЕЙНЕР
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    // Если изображение загружено - высота автоматическая, иначе фиксированная
                    if (imageBitmap != null) {
                        Modifier.wrapContentHeight()
                    } else {
                        Modifier.height(200.dp)
                    }
                )
                .clip(RoundedCornerShape(25.dp))
                .then(
                    // Серый фон только если изображения нет
                    if (imageBitmap == null) {
                        Modifier.background(Color(0xFFE0E0E0))
                    } else {
                        Modifier
                    }
                )
                .then(
                    // Клик: в режиме редактирования - загрузка, в режиме просмотра - открыть полноэкранный просмотр
                    if (isEditing) {
                        Modifier.clickable { onUploadClick() }
                    } else if (imageBitmap != null) {
                        Modifier.clickable { showFullScreen = true }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = TrackMePurple)
                }
                hasError -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ImageNotSupported,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Text("Не удалось загрузить", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                imageBitmap != null -> {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Скриншот встречи",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentScale = ContentScale.FillWidth  // ← Заполняет по ширине, высота автоматическая
                    )
                }
                else -> {
                    // Placeholder когда изображения нет
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
                                text = getScreenshotPlaceholderText(isEditing),
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
                                getScreenshotPlaceholderText(isEditing),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // ПОЛНОЭКРАННЫЙ ПРОСМОТР
        if (showFullScreen && imageBitmap != null) {
            FullScreenImageViewer(
                imageBitmap = imageBitmap!!,
                onDismiss = { showFullScreen = false }
            )
        }
    }
}

// НОВЫЙ COMPOSABLE: Полноэкранный просмотр изображения
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageViewer(
    imageBitmap: ImageBitmap,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val animatedScale = animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 20)
    )
    val animatedOffsetX = animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 20)
    )
    val animatedOffsetY = animateFloatAsState(
        targetValue = offsetY,
        animationSpec = tween(durationMillis = 20)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val maxWidth = constraints.maxWidth.toFloat()
            val maxHeight = constraints.maxHeight.toFloat()

            // Вычисляем пределы смещения с учетом zoom
            fun getMaxOffset(): Pair<Float, Float> {
                if (scale <= 1f) return 0f to 0f

                val scaledWidth = maxWidth * scale
                val scaledHeight = maxHeight * scale

                val maxX = ((scaledWidth - maxWidth) / 2f).coerceAtLeast(0f)
                val maxY = ((scaledHeight - maxHeight) / 2f).coerceAtLeast(0f)

                return maxX to maxY
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        // Pinch-to-zoom и панорамирование
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 4f)

                            // Если приближаем - разрешаем панорамирование
                            if (newScale > 1f) {
                                val (maxX, maxY) = getMaxOffset()

                                offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                                offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                            } else {
                                // Если уменьшили до 1x - сбрасываем offset
                                offsetX = 0f
                                offsetY = 0f
                            }

                            scale = newScale
                        }
                    }
                    .pointerInput(Unit) {
                        // Двойной тап для zoom
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                if (scale > 1f) {
                                    // Если увеличено - сбрасываем
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    // Если обычный размер - увеличиваем в 2x
                                    scale = 2.5f

                                    // Центрируем на точке тапа
                                    val focusX = (tapOffset.x - maxWidth / 2) * scale
                                    val focusY = (tapOffset.y - maxHeight / 2) * scale

                                    val (maxX, maxY) = getMaxOffset()
                                    offsetX = (-focusX).coerceIn(-maxX, maxX)
                                    offsetY = (-focusY).coerceIn(-maxY, maxY)
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        // Свайп для закрытия (только при scale = 1)
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (scale <= 1f) {
                                    onDismiss()
                                }
                            }
                        ) { change, dragAmount ->
                            if (scale <= 1f) {
                                change.consume()
                                if (kotlin.math.abs(dragAmount) > 150) {
                                    onDismiss()
                                }
                            }
                        }
                    }
            ) {
                // Изображение
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Полноэкранный просмотр",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = animatedScale.value,
                            scaleY = animatedScale.value,
                            translationX = animatedOffsetX.value,
                            translationY = animatedOffsetY.value
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            // Кнопка закрытия
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}