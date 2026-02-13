package com.example.track_me_mobile.features.auth.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.domain.AuthError
import com.example.track_me_mobile.features.auth.domain.models.CsrfToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class AuthRepositoryImpl(private val client: HttpClient) : AuthRepository {

    override suspend fun getCsrfToken(): Result<CsrfToken> = try {
        val response: CsrfResponse = client.get(ApiConstants.CSRF_ENDPOINT).body()
        Result.success(CsrfToken(response.token, response.parameterName))
    } catch (e: Exception) {
        Result.failure(AuthError.NetworkError)
    }

    override suspend fun login(username: String, password: String, csrf: CsrfToken): Result<Unit> = try {
        val response = client.submitForm(
            url = ApiConstants.LOGIN_ENDPOINT,
            formParameters = parameters {
                append("username", username)
                append("password", password)
                append(csrf.parameterName, csrf.token)
            }
        ) {
            header("X-CSRF-TOKEN", csrf.token)
        }

        val redirectUrl = response.headers["Location"] ?: ""

        when {
            // Если редирект содержит error - значит логин не прошел
            redirectUrl.contains("error") -> Result.failure(AuthError.InvalidCredentials)

            // Если код 200-302 и нет ошибки в URL - успех
            response.status.value in 200..302 -> Result.success(Unit)

            // Все остальное - ошибка сервера
            else -> Result.failure(AuthError.ServerError)
        }
    } catch (e: Exception) {
        Result.failure(AuthError.NetworkError)
    }
}