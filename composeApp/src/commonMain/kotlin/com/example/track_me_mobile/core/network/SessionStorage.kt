package com.example.track_me_mobile.core.network

import io.ktor.client.plugins.cookies.*
import io.ktor.http.*

// Отвечает за хранение SESSION и передачу его в Ktor.
// Изолирует HttpClientFactory от деталей работы с сессией.
class SessionStorage(
    val cookieStorage: AcceptAllCookiesStorage = AcceptAllCookiesStorage()
) {
    private val gatewayUrl = Url("https://${ApiConstants.GATEWAY_HOST}")

    suspend fun save(sessionValue: String) {
        cookieStorage.addCookie(
            gatewayUrl,
            Cookie(
                name     = "SESSION",
                value    = sessionValue,
                domain   = ApiConstants.GATEWAY_HOST,
                path     = "/",
                secure   = true,
                httpOnly = true
            )
        )
    }

    suspend fun get(): String? =
        cookieStorage.get(gatewayUrl).find { it.name == "SESSION" }?.value

    suspend fun clear() {
        // При выходе из аккаунта — очищаем куку
        cookieStorage.get(gatewayUrl)
            .filter { it.name == "SESSION" }
            .forEach {
                cookieStorage.addCookie(
                    gatewayUrl,
                    it.copy(value = "", maxAge = 0)
                )
            }
    }
}