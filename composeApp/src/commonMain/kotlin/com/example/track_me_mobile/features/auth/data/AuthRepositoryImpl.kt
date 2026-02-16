package com.example.track_me_mobile.features.auth.data

import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.core.network.logDebug
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.auth.data.model.UserInfoDto
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.domain.AuthError
import com.example.track_me_mobile.features.auth.domain.models.CsrfToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val client: HttpClient,
    private val storage: CookiesStorage
) : AuthRepository {

    private var currentSessionCookie: String? = null

    override suspend fun syncSession(cookieString: String) {
        // Берем только SESSION=... без лишних параметров
        currentSessionCookie = cookieString.split(";")[0].trim()
        logDebug("DEBUG_TAG: Сессия синхронизирована: $currentSessionCookie")
    }

    override suspend fun getUserInfo(): Result<UserInfo> = try {
        // 1. Получаем CSRF (используя сессию)
        val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
            if (currentSessionCookie != null) header("Cookie", currentSessionCookie)
            header("Accept", "application/json") // Просим JSON
        }

        if (csrfResponse.status != HttpStatusCode.OK) {
            throw Exception("Ошибка CSRF: ${csrfResponse.status}")
        }

        val csrf = csrfResponse.body<CsrfResponse>()

        // 2. Получаем профиль
        val response = client.get("api/v1/account/info") {
            if (currentSessionCookie != null) header("Cookie", currentSessionCookie)
            header(csrf.parameterName, csrf.token)
            header("Accept", "application/json")
        }

        val bodyText = response.bodyAsText()
        logDebug("DEBUG_TAG: Ответ сервера: ${response.status}")

        if (response.status == HttpStatusCode.OK && bodyText.startsWith("{")) {
            val dto = Json.decodeFromString<UserInfoDto>(bodyText)
            val roles = dto.roles.map { roleStr ->
                try {
                    Role.valueOf(roleStr.replace("ROLE_", "").uppercase())
                } catch (e: Exception) {
                    Role.UNKNOWN
                }
            }
            Result.success(UserInfo(roles))
        } else {
            Result.failure(Exception("Сессия не принята (Код ${response.status})"))
        }
    } catch (e: Exception) {
        logDebug("DEBUG_TAG: Ошибка getUserInfo: ${e.message}")
        Result.failure(e)
    }

    override suspend fun getCsrfToken(): Result<CsrfToken> = Result.failure(Exception("Not used"))
    override suspend fun login(username: String, password: String, csrf: CsrfToken): Result<Unit> = Result.success(Unit)
}