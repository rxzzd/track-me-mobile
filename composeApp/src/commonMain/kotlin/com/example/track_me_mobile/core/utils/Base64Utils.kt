package com.example.track_me_mobile.core.utils

expect object Base64Utils {
    fun encodeToString(bytes: ByteArray): String
    fun decodeFromString(string: String): ByteArray
}