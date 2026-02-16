package com.example.track_me_mobile.core.network

import com.multiplatform.webview.web.NativeWebView

actual fun getSyncCookies(url: String): String? = null // Для iOS пока просто возвращаем null
actual fun logDebug(message: String) {
    println("IOS_LOG: $message")
}

actual fun configureNativeWebView(webView: NativeWebView) {
    // На iOS эти настройки либо не нужны, либо делаются иначе.
    // Пока оставляем пустым.
}
actual fun clearWebViewCookies() {} // Заглушка