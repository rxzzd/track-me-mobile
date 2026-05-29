package com.example.track_me_mobile.testsupport

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun testHttpClient(engine: MockEngine): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }
        )
    }
}

fun csrfJson(token: String = "csrf-token", headerName: String = "X-CSRF-TOKEN"): String =
    """{"token":"$token","headerName":"$headerName","parameterName":"_csrf"}"""
