package com.example.track_me_mobile.core.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(cookieStorage: CookiesStorage): HttpClient {
        return HttpClient {
            // ВАЖНО: Запрещаем Ktor'у самому бегать по ссылкам перенаправления
            followRedirects = false

            install(HttpCookies) {
                storage = cookieStorage
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 10000
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }, contentType = ContentType.Any)
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        // Используем тот же логгер, что и в репозитории для единообразия
                        logDebug("NETWORK_LOG: $message")
                    }
                }
                level = LogLevel.INFO
            }

            defaultRequest {
                url(ApiConstants.BASE_URL)
                // Говорим серверу: "Мы хотим JSON, не шли нам HTML!"
                header("Accept", "application/json")
            }
        }
    }
}