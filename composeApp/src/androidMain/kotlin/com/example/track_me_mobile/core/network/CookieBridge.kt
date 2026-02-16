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
    // В Android модуле NativeWebView — это и есть android.webkit.WebView
    val cookieManager = android.webkit.CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    cookieManager.setAcceptThirdPartyCookies(webView, true)

    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        // Это самое важное для устранения ERR_HTTP_RESPONSE_CODE_FAILURE
        userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
        cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
    }

    // Добавляем клиент ПРЯМО ЗДЕСЬ, чтобы не "краснило" в общем коде
    webView.webViewClient = object : android.webkit.WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: android.webkit.WebView?,
            request: android.webkit.WebResourceRequest?
        ): Boolean {
            // Разрешаем WebView самому обрабатывать редиректы
            return false
        }

        // Поможет отладить, если ошибка останется
        override fun onReceivedHttpError(
            view: android.webkit.WebView?,
            request: android.webkit.WebResourceRequest?,
            errorResponse: android.webkit.WebResourceResponse?
        ) {
            logDebug("WEBVIEW_ERROR: Код ${errorResponse?.statusCode} на URL ${request?.url}")
        }
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