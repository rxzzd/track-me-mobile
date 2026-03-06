package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(
    onImageSelected: (ByteArray) -> Unit
): () -> Unit