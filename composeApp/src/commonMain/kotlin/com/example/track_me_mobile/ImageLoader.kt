package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun loadImageBitmap(bytes: ByteArray): ImageBitmap?