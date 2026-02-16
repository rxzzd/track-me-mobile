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
            if (url.isEmpty()) return@LaunchedEffect

            logDebug("WEBVIEW_TRACE: Проверяем URL -> $url")

            // СУДЯ ПО СКРИНШОТАМ: Успех - это когда нас перекинуло на after-login
            if (url.contains("/after-login")) {
                logDebug("WEBVIEW_MATCH: Обнаружен финишный редирект!")

                // Ждем немного, чтобы куки точно записались
                delay(500)

                // ВАЖНО: Финальную сессию ставит домен API, а не тот домен, на котором after-login
                // Поэтому забираем куки с API домена
                val apiCookies = getSyncCookies("https://api.trackme.test.startup-poligon.com")

                if (apiCookies != null && apiCookies.contains("SESSION=")) {
                    logDebug("WEBVIEW_SUCCESS: Сессия найдена, завершаем вход.")
                    onFinish(apiCookies)
                } else {
                    logDebug("WEBVIEW_ERROR: URL верный, но куки SESSION нет. Куки: $apiCookies")
                }
            }
        }



        WebView(
            state = state,
            modifier = Modifier.fillMaxSize(),
            onCreated = { webView ->
                // Просто вызываем нашу функцию.
                // Вся магия с переопределением клиента будет внутри неё в androidMain.
                configureNativeWebView(webView)
            }
        )
    }
}