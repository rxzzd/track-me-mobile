package com.example.track_me_mobile.core.utils

object ImageUtils {
    fun createDataUri(imageBytes: ByteArray): String {
        val base64 = Base64Utils.encodeToString(imageBytes)
        return "data:image/jpeg;base64,$base64"
    }

    fun extractBase64(dataUri: String): ByteArray? {
        return try {
            if (!dataUri.startsWith("data:image/")) return null
            val base64 = dataUri.substringAfter("base64,")
            Base64Utils.decodeFromString(base64)
        } catch (e: Exception) {
            println("[ImageUtils] Failed to decode data URI: ${e.message}")
            null
        }
    }
}