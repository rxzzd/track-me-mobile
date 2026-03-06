package com.example.track_me_mobile.core.network

import io.ktor.client.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.ContentType
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.*

object HttpClientFactory {

    fun create(sessionStorage: SessionStorage): HttpClient {
        return HttpClient {
            // Плагин сам берёт SESSION из sessionStorage и подставляет в каждый запрос
            install(HttpCookies) {
                storage = sessionStorage.cookieStorage
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }, contentType = ContentType.Any)
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        if (message.contains("/image/")) {
                            println("[HTTP-Image] $message")
                        }
                    }
                }
                level = LogLevel.HEADERS
            }

            followRedirects = false
        }
    }
}