package com.example.track_me_mobile.features.auth.data

import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.core.network.SessionStorage
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.auth.data.model.UserInfoDto
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class AuthRepositoryImpl(
    private val client: HttpClient,
    private val sessionStorage: SessionStorage
) : AuthRepository {

    // Вызывается из ViewModel после получения SESSION из WebView.
    // Сохраняет сессию в хранилище — Ktor плагин подставит её автоматически.
    suspend fun saveSession(cookieString: String) {
        val sessionValue = cookieString
            .split(";")
            .map { it.trim() }
            .find { it.startsWith("SESSION=") }
            ?.substringAfter("SESSION=")
            ?.trim()
            ?: return

        sessionStorage.save(sessionValue)
    }

    override suspend fun getUserInfo(): Result<UserInfo> {
        return try {
            // ШАГ 1: CSRF — SESSION подставляется плагином HttpCookies автоматически
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }

            if (csrfResponse.status != HttpStatusCode.OK) {
                return Result.failure(Exception("CSRF вернул статус ${csrfResponse.status}"))
            }

            val csrfText = csrfResponse.bodyAsText()
            if (csrfText.trim().startsWith("<")) {
                return Result.failure(Exception("Сессия истекла или недействительна"))
            }

            val csrfData = csrfResponse.body<CsrfResponse>()

            // ШАГ 2: Профиль пользователя
            val profileResponse = client.get(ApiConstants.ACCOUNT_INFO) {
                header(HttpHeaders.Accept,   "application/json")
                header(csrfData.headerName,  csrfData.token)
                header("X-Requested-With",   "XMLHttpRequest")
            }

            if (profileResponse.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Ошибка профиля: ${profileResponse.status}"))
            }

            val profileText = profileResponse.bodyAsText()
            if (profileText.trim().startsWith("<")) {
                return Result.failure(Exception("Сервер вернул неожиданный ответ"))
            }

            val dto = profileResponse.body<UserInfoDto>()

            Result.success(
                UserInfo(
                    id       = dto.id,
                    username = dto.username,
                    fullName = dto.fullName,
                    roles    = dto.roles.map { mapRole(it) }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapRole(raw: String): Role = when (raw.uppercase()) {
        "SUPER_ADMIN", "ROLE_SUPER_ADMIN" -> Role.SUPER_ADMIN
        "ADMIN",       "ROLE_ADMIN"       -> Role.ADMIN
        "TRACKER",     "ROLE_TRACKER"     -> Role.TRACKER
        else                              -> Role.UNKNOWN
    }
}