package com.example.track_me_mobile.core.network

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.util.Log
import android.webkit.WebSettings

import com.multiplatform.webview.web.NativeWebView

actual fun clearWebViewCookies() {
    val cookieManager = CookieManager.getInstance()
    cookieManager.removeAllCookies {
        cookieManager.flush()
    }
}

@SuppressLint("SetJavaScriptEnabled")
actual fun configureNativeWebView(webView: NativeWebView) {
    // Подготавливаем менеджер кук ПЕРЕД настройкой WebView
    val cookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    cookieManager.setAcceptThirdPartyCookies(webView, true)

    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        // Маскируемся под чистый Chrome
        userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    }
}

actual fun logDebug(message: String) {
    Log.d("TRACKME_SYSTEM", message)
}
// Реализация для Android
actual fun getSyncCookies(url: String): String? {
    val cookieManager = CookieManager.getInstance()
    // Принудительно сохраняем куки на диск перед чтением
    cookieManager.flush()
    return cookieManager.getCookie(url)
}