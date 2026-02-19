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
            val cookieManager = CookieManager.getInstance().apply {
                setAcceptCookie(true)
            }

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
                    private var codeCallbackReceived = false

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString() ?: return false
                        val uri = request.url

                        // Строгая проверка code callback: хост шлюза + путь /login/oauth2/code/
                        // (url.contains() ловил бы этот путь внутри redirect_uri= у SSO)
                        val isCodeCallback = uri.host == ApiConstants.GATEWAY_HOST
                                && uri.path?.startsWith("/login/oauth2/code/") == true

                        if (isCodeCallback) {
                            codeCallbackReceived = true
                        }

                        // После обмена code на токены шлюз редиректит на фронт.
                        // Перехватываем — не даём WebView уйти на веб-сайт.
                        val isAfterLogin = url.contains("/after-login")
                        if (isAfterLogin && codeCallbackReceived && !isLoginHandled) {
                            handleAuthSuccess()
                            return true
                        }

                        return false
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (url == null || isLoginHandled || !codeCallbackReceived) return

                        // Запасной вариант: если after-login не поймали в shouldOverride
                        if (url.contains(ApiConstants.GATEWAY_HOST)) {
                            handleAuthSuccess()
                        }
                    }

                    private fun handleAuthSuccess() {
                        if (isLoginHandled) return

                        val gatewayCookies = CookieManager.getInstance()
                            .getCookie("https://${ApiConstants.GATEWAY_HOST}")

                        if (gatewayCookies?.contains("SESSION=") == true) {
                            isLoginHandled = true
                            viewModel.loginFromWebView(gatewayCookies) { role ->
                                when (role) {
                                    Role.ADMIN, Role.SUPER_ADMIN -> navigator.replaceAll(StreamListScreen())
                                    Role.TRACKER                -> navigator.replaceAll(TeamListScreen())
                                    else                        -> navigator.pop()
                                }
                            }
                        }
                    }
                }

                // Сбрасываем состояние WebView перед каждым входом
                clearCache(true)
                clearHistory()
                clearFormData()
                android.webkit.WebStorage.getInstance().deleteAllData()

                cookieManager.removeAllCookies {
                    cookieManager.flush()
                    postDelayed({ loadUrl(ApiConstants.AUTH_TRIGGER_URL) }, 200)
                }
            }
        })
    }
}