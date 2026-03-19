package com.example.track_me_mobile.core.utils

import android.util.Base64

actual object Base64Utils {
    actual fun encodeToString(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    actual fun decodeFromString(string: String): ByteArray {
        return Base64.decode(string, Base64.DEFAULT)
    }
}