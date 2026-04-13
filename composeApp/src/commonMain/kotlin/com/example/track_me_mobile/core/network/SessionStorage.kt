package com.example.track_me_mobile.core.network

import com.example.track_me_mobile.core.storage.PersistentStorage
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*

class SessionStorage(
    private val persistentStorage: PersistentStorage,
    val cookieStorage: AcceptAllCookiesStorage = AcceptAllCookiesStorage()
) {
    private val gatewayUrl = Url("https://${ApiConstants.GATEWAY_HOST}")

    companion object {
        private const val SESSION_KEY = "session_cookie"
    }

    suspend fun save(sessionValue: String) {
        println("[SessionStorage] Saving SESSION to persistent storage")

        persistentStorage.saveString(SESSION_KEY, sessionValue)

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

    suspend fun get(): String? {
        val memorySession = cookieStorage.get(gatewayUrl)
            .find { it.name == "SESSION" }?.value

        if (memorySession != null) {
            println("[SessionStorage] Found SESSION in memory")
            return memorySession
        }

        val persistedSession = persistentStorage.getString(SESSION_KEY)

        if (persistedSession != null) {
            println("[SessionStorage] Restoring SESSION from persistent storage")

            cookieStorage.addCookie(
                gatewayUrl,
                Cookie(
                    name     = "SESSION",
                    value    = persistedSession,
                    domain   = ApiConstants.GATEWAY_HOST,
                    path     = "/",
                    secure   = true,
                    httpOnly = true
                )
            )

            return persistedSession
        }

        println("[SessionStorage] No SESSION found")
        return null
    }

    suspend fun clear() {
        println("[SessionStorage] Clearing SESSION")

        persistentStorage.remove(SESSION_KEY)

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