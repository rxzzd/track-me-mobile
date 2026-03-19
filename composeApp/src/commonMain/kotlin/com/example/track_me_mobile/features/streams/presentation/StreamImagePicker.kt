package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.features.meetings.presentation.loadImageBitmap
import kotlinx.coroutines.launch

@Composable
fun StreamImagePickerBlock(
    existingImageBytes: ByteArray?,
    pendingImageBytes: ByteArray?,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Показываем pending или existing
    val bytesToDisplay = pendingImageBytes ?: existingImageBytes

    LaunchedEffect(bytesToDisplay) {
        displayBitmap = bytesToDisplay?.let { loadImageBitmap(it) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE0E0E0))
            .clickable { onPickImage() }
            // Padding ТОЛЬКО если нет фото
            .then(
                if (displayBitmap == null) Modifier.padding(16.dp)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (displayBitmap != null) {
            Image(
                bitmap = displayBitmap!!,
                contentDescription = "Фото потока",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.FillWidth
            )

            // Индикатор нового фото
            if (pendingImageBytes != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = TrackMePurple,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Новое фото",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Прикрепить фотографию",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}