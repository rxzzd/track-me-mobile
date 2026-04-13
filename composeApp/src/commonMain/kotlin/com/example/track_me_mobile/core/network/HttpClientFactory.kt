package com.example.track_me_mobile.core.network

import io.ktor.client.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.observer.ResponseObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object HttpClientFactory {

    fun create(
        sessionStorage: SessionStorage,
        onUnauthorized: (() -> Unit)? = null
    ): HttpClient {
        return HttpClient {
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

            install(ResponseObserver) {
                onResponse { response ->
                    if (response.status == HttpStatusCode.Unauthorized) {
                        println("[HTTP] 401 Unauthorized - refresh_token expired, clearing session")

                        CoroutineScope(Dispatchers.Main).launch {
                            sessionStorage.clear()
                            onUnauthorized?.invoke()
                        }
                    }
                }
            }

            followRedirects = false
        }
    }
}