package com.example.track_me_mobile.features.auth.data

import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.core.network.HttpClientFactory
import com.example.track_me_mobile.core.network.logDebug
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.auth.data.model.UserInfoDto
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.domain.models.CsrfToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val client: HttpClient
) : AuthRepository {

    private var currentSessionCookie: String? = null

    // В AuthRepositoryImpl.kt

    override suspend fun syncSession(cookieString: String) {
        // 1. Парсим строку кук (например: "SESSION=123-abc; Path=/; HttpOnly")
        // Для простоты вытащим значение SESSION
        val sessionValue = cookieString
            .split(";")
            .find { it.trim().startsWith("SESSION=") }
            ?.substringAfter("=")

        if (sessionValue != null) {
            // 2. Добавляем куку напрямую в хранилище Ktor
            HttpClientFactory.sessionCookieStorage.addCookie(
                Url(ApiConstants.BASE_URL),
                Cookie(name = "SESSION", value = sessionValue, domain = "sso.trackme.test.startup-poligon.com")
            )
            logDebug("DEBUG_TAG: Кука SESSION успешно внедрена в Ktor")
        }
    }

    override suspend fun getUserInfo(): Result<UserInfo> {
        return try {
            // Используем сессию для получения CSRF
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                // Куки теперь подставляются автоматически из sessionCookieStorage
                header("Accept", "application/json")
            }

            // Если всё еще 302 или 401 — значит сессия все-таки "битая"
            if (csrfResponse.status == HttpStatusCode.Found || csrfResponse.status == HttpStatusCode.Unauthorized) {
                logDebug("DEBUG_TAG: Сессия невалидна (сервер просит логин)")
                return Result.failure(Exception("Требуется повторная авторизация"))
            }

            val csrf = csrfResponse.body<CsrfResponse>()

            // Получаем профиль с кукой И CSRF-токеном
            val response = client.get("api/v1/account/info") {
                currentSessionCookie?.let { header(HttpHeaders.Cookie, it) }
                header(csrf.parameterName, csrf.token)
            }

            if (response.status == HttpStatusCode.OK) {
                val dto = response.body<UserInfoDto>()
                val roles = dto.roles.map { roleStr ->
                    try {
                        Role.valueOf(roleStr.replace("ROLE_", "").uppercase())
                    } catch (e: Exception) {
                        Role.UNKNOWN
                    }
                }
                Result.success(UserInfo(roles))
            } else {
                Result.failure(Exception("Сервер вернул ${response.status}"))
            }
        } catch (e: Exception) {
            logDebug("DEBUG_TAG: Ошибка в getUserInfo: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getCsrfToken(): Result<CsrfToken> = Result.failure(Exception("Not used"))
    override suspend fun login(username: String, password: String, csrf: CsrfToken): Result<Unit> = Result.success(Unit)
}