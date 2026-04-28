package com.example.track_me_mobile.core.files

import android.content.Context
import android.os.Environment
import java.io.File

actual suspend fun saveExcelFile(fileName: String, bytes: ByteArray): Boolean {
    return try {
        val context: Context = AppContextHolder.context
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        val safeFileName = fileName.replace("[\\/:*?\"<>|]".toRegex(), "_")
        val file = File(downloadsDir, safeFileName)
        file.outputStream().use { it.write(bytes) }
        println("[SAVE_EXCEL] Сохранено: ${file.absolutePath}")
        true
    } catch (e: Exception) {
        println("[SAVE_EXCEL] Ошибка сохранения: ${e.message}")
        false
    }
}
