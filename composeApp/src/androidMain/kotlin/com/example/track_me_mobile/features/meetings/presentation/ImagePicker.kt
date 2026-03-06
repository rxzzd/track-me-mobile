package com.example.track_me_mobile.features.meetings.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePicker(
    onImageSelected: (ByteArray) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // TODO: Конвертировать Uri в ByteArray и вызвать onImageSelected
            println("[ImagePicker] Selected: $selectedUri")
        }
    }

    return {
        launcher.launch("image/*")
    }
}