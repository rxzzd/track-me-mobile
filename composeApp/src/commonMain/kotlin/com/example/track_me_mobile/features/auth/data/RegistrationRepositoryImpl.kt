package com.example.track_me_mobile.features.auth.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.auth.data.model.RegistrationRequest
import com.example.track_me_mobile.features.auth.domain.RegistrationRepository
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import io.ktor.client.call.*

class RegistrationRepositoryImpl(
    private val client: HttpClient
) : RegistrationRepository {

    override suspend fun initRegistration(
        username: String,
        password: String,
        phoneNumber: String,
        fullName: String,
        email: String,
        role: String
    ): Result<Unit> {
        return try {
            // Шаг 1: получаем CSRF
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            val csrfData = csrfResponse.body<CsrfResponse>()
            println("[REG] CSRF получен: ${csrfData.token.take(20)}...")

            // Шаг 2: регистрация с CSRF в заголовке
            val response = client.post(ApiConstants.REGISTRATION_INIT) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Accept,   "application/json")
                header("X-Requested-With", "XMLHttpRequest")
                header(csrfData.headerName,  csrfData.token)   // X-CSRF-TOKEN
                setBody(RegistrationRequest(
                    username    = username,
                    password    = password,
                    phoneNumber = phoneNumber,
                    fullName    = fullName,
                    email       = email,
                    role        = role
                ))
            }

            println("[REG] Final URL: ${response.request.url}")

            if (response.request.url.toString().contains("/login")) {
                return Result.failure(Exception("Session/CSRF rejected. Redirected to login."))
            }

            val body = response.bodyAsText()
            val location = response.headers["Location"]
            println("[REG] Статус: ${response.status}")
            println("[REG] Location: $location")
            println("[REG] Тело: ${body.take(300)}")

            if (response.status != HttpStatusCode.OK) {
                println("[REG] Ошибка от сервера: $body")
            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.status}. Тело: $body"))
            }
        } catch (e: Exception) {
            println("[REG] ИСКЛЮЧЕНИЕ: ${e::class.simpleName} — ${e.message}")
            Result.failure(e)
        }
    }
}