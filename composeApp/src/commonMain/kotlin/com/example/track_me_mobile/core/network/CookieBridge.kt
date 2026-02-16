package com.example.track_me_mobile.core.network

import com.multiplatform.webview.web.NativeWebView

expect fun logDebug(message: String)
expect fun getSyncCookies(url: String): String?

expect fun clearWebViewCookies()

expect fun configureNativeWebView(webView: NativeWebView)