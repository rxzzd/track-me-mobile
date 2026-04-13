package com.example.track_me_mobile.features.meetings.presentation

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.InputStream

@Composable
actual fun rememberImagePicker(
    onImageSelected: (ByteArray) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                println("[ImagePicker] Selected: $selectedUri")

                val bytes = uriToByteArray(context, selectedUri)

                if (bytes != null) {
                    println("[ImagePicker] Converted to ${bytes.size} bytes")
                    onImageSelected(bytes)
                } else {
                    println("[ImagePicker] Failed to read image")
                }
            } catch (e: Exception) {
                println("[ImagePicker] Error reading image: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    return {
        println("[ImagePicker] Launching image picker...")
        launcher.launch("image/*")
    }
}

private fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { it.readBytes() }
    } catch (e: Exception) {
        println("[ImagePicker] Error converting Uri to ByteArray: ${e.message}")
        null
    }
}