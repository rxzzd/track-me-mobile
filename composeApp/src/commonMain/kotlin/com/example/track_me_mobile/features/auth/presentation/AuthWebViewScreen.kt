package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import com.example.track_me_mobile.core.network.getSyncCookies
import com.example.track_me_mobile.core.network.logDebug
import com.example.track_me_mobile.core.network.configureNativeWebView
import kotlinx.coroutines.delay

class AuthWebViewScreen(private val onFinish: (String) -> Unit) : Screen {
    @Composable
    override fun Content() {
        // Используем триггер, который ты присылал
        val triggerUrl = "https://api.trackme.test.startup-poligon.com/sso/oauth2/authorization/track-me-client"
        val state = rememberWebViewState(triggerUrl)

        LaunchedEffect(state.lastLoadedUrl) {
            val url = state.lastLoadedUrl ?: ""
            if (url.isNotEmpty()) {
                logDebug("WEBVIEW_TRACE: Сейчас на -> $url")
            }

            // ЖЕСТКОЕ УСЛОВИЕ УСПЕХА:
            // Закрываемся ТОЛЬКО если в адресе есть /after-login
            if (url.contains("/after-login")) {
                logDebug("WEBVIEW_TRACE: ФИНАЛ ДОСТИГНУТ! Собираем куки...")

                // Даем время браузеру прописать куку в системную память
                delay(1000)

                // Берем куку именно у API домена
                val apiCookies = getSyncCookies("https://api.trackme.test.startup-poligon.com")

                if (apiCookies != null && apiCookies.contains("SESSION")) {
                    logDebug("WEBVIEW_TRACE: Авторизованная сессия получена. Выходим.")
                    onFinish(apiCookies)
                } else {
                    logDebug("WEBVIEW_TRACE: ОШИБКА - Мы на after-login, но SESSION нет!")
                }
            }
        }

        WebView(
            state = state,
            modifier = Modifier.fillMaxSize(),
            onCreated = { webView ->
                configureNativeWebView(webView)
            }
        )
    }
}