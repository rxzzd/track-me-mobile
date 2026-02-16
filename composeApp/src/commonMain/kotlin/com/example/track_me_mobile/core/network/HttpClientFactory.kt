package com.example.track_me_mobile.core.network

import io.ktor.client.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.ContentType
import kotlinx.serialization.json.Json

object HttpClientFactory {
    // 1. ВОТ ЗДЕСЬ мы объявляем переменную. Она должна быть внутри object, но вне функций.
    val sessionCookieStorage = AcceptAllCookiesStorage()

    fun create(): HttpClient {
        return HttpClient {
            install(HttpCookies) {
                // 2. Теперь IDE увидит эту переменную
                storage = sessionCookieStorage
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }, contentType = ContentType.Any)
            }

            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        // Используй свой логгер
                        println("HTTP_CLIENT: $message")
                    }
                }
            }
        }
    }
}