package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.track_me_mobile.features.meetings.presentation.loadImageBitmap
import org.jetbrains.compose.resources.painterResource
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.base_stream_photo

@Composable
fun StreamCardImage(
    imageBytes: ByteArray?,
    modifier: Modifier = Modifier
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(imageBytes) {
        imageBitmap = imageBytes?.let { loadImageBitmap(it) }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            // Показываем загруженное фото
            Image(
                bitmap = imageBitmap!!,
                contentDescription = "Фото потока",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Показываем placeholder (base_stream_photo)
            Image(
                painter = painterResource(Res.drawable.base_stream_photo),
                contentDescription = "Фото по умолчанию",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}