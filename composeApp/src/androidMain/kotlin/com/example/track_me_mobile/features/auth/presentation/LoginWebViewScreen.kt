package com.example.track_me_mobile.features.auth.presentation

import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.streams.presentation.StreamListScreen
import com.example.track_me_mobile.features.teams.presentation.TeamListScreen

class LoginWebViewScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<LoginViewModel>()

        AndroidView(factory = { context ->
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/121.0.0.0 Safari/537.36"

                webViewClient = object : WebViewClient() {

                    private var isLoginHandled = false

                    // Флаг: шлюз уже получил ?code= и начал его обменивать
                    // Только после этого момента SESSION становится "настоящим"
                    private var codeCallbackReceived = false

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString() ?: return false
                        val uri = request.url

                        android.util.Log.d("TRACKME_WEBVIEW", "→ Редирект на: $url")

                        // Проверяем строго: хост — шлюз, путь начинается с /login/oauth2/code/
                        // (НЕ path.contains — иначе ловим redirect_uri= в query-параметрах SSO)
                        val isCodeCallback = uri.host == ApiConstants.GATEWAY_HOST
                                && uri.path?.startsWith("/login/oauth2/code/") == true

                        if (isCodeCallback) {
                            android.util.Log.d("TRACKME_WEBVIEW", "✓ Настоящий code callback! code=${uri.getQueryParameter("code")?.take(20)}...")
                            codeCallbackReceived = true
                        }

                        // Финальный редирект после успешного обмена токенов — шлюз отправляет
                        // пользователя на фронт (/after-login или /streams).
                        // Перехватываем здесь, не даём WebView уходить на веб-сайт.
                        val isAfterLogin = url.contains("/after-login") || url.contains("trackme.test.startup-poligon.com/streams")
                        if (isAfterLogin && codeCallbackReceived && !isLoginHandled) {
                            android.util.Log.d("TRACKME_WEBVIEW", "✓ Финальный редирект перехвачен: $url")
                            handleAuthSuccess()
                            return true // Останавливаем WebView — не открываем веб-сайт
                        }

                        return false
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (url == null || isLoginHandled) return

                        android.util.Log.d("TRACKME_WEBVIEW", "Страница загружена: $url | codeReceived=$codeCallbackReceived")

                        // Если шлюз завершил обмен токенов и мы оказались на любой его странице
                        if (codeCallbackReceived && url.contains(ApiConstants.GATEWAY_HOST)) {
                            handleAuthSuccess()
                        }
                    }

                    private fun handleAuthSuccess() {
                        if (isLoginHandled) return

                        // SESSION берём с домена шлюза — именно он его выдаёт
                        val gatewayCookies = CookieManager.getInstance()
                            .getCookie("https://${ApiConstants.GATEWAY_HOST}")

                        android.util.Log.d("TRACKME_WEBVIEW", "Куки шлюза: $gatewayCookies")

                        if (gatewayCookies?.contains("SESSION=") == true) {
                            android.util.Log.i("TRACKME_WEBVIEW", "🚀 SESSION получен после обмена токенов!")
                            isLoginHandled = true

                            viewModel.loginFromWebView(gatewayCookies) { role ->
                                when (role) {
                                    Role.ADMIN, Role.SUPER_ADMIN -> navigator.replaceAll(TeamListScreen())
                                    Role.TRACKER                -> navigator.replaceAll(StreamListScreen())
                                    else                        -> navigator.pop()
                                }
                            }
                        } else {
                            android.util.Log.w("TRACKME_WEBVIEW", "⚠ Ожидали SESSION, но его нет. Куки: $gatewayCookies")
                        }
                    }
                }

                // Очищаем состояние перед стартом
                clearCache(true)
                clearHistory()
                clearFormData()
                android.webkit.WebStorage.getInstance().deleteAllData()

                cookieManager.removeAllCookies {
                    cookieManager.flush()
                    postDelayed({
                        android.util.Log.d("TRACKME_WEBVIEW", "Старт OAuth: ${ApiConstants.AUTH_TRIGGER_URL}")
                        loadUrl(ApiConstants.AUTH_TRIGGER_URL)
                    }, 200)
                }
            }
        })
    }
}