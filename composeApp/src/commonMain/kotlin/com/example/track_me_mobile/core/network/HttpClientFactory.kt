package com.example.track_me_mobile.core.network

import io.ktor.client.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.ContentType
import kotlinx.serialization.json.Json

object HttpClientFactory {
    val sessionCookieStorage = AcceptAllCookiesStorage()

    fun create(): HttpClient {
        return HttpClient {
            // HttpCookies сам читает хранилище и подставляет нужные куки в каждый запрос.
            // Не нужно вручную добавлять header(Cookie, ...) — это создаёт конфликт.
            install(HttpCookies) {
                storage = sessionCookieStorage
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }, contentType = ContentType.Any)
            }
        }
    }
}