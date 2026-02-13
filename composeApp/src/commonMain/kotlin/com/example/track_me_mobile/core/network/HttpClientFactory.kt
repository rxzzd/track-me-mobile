package com.example.track_me_mobile.core.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(): HttpClient {
        return HttpClient {
            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
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
                        napierLog(message)
                    }
                }
                level = LogLevel.INFO
            }

            defaultRequest {
                url(ApiConstants.BASE_URL)
            }
        }
    }

    private fun napierLog(message: String) {
        println("NETWORK_LOG: $message")
    }
}