package com.example.track_me_mobile.features.auth.data

import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.core.network.HttpClientFactory
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.auth.data.model.UserInfoDto
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.domain.models.CsrfToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class AuthRepositoryImpl(
    private val client: HttpClient
) : AuthRepository {

    companion object {
        private const val TAG = "TRACKME_REPO"
    }

    override suspend fun syncSession(cookieString: String) {
        log("syncSession → raw cookie string: $cookieString")

        val sessionValue = cookieString
            .split(";")
            .map { it.trim() }
            .find { it.startsWith("SESSION=") }
            ?.substringAfter("SESSION=")
            ?.trim()

        if (sessionValue.isNullOrBlank()) {
            log("syncSession ✗ SESSION не найден в строке куки!")
            return
        }

        // Добавляем куку в хранилище Ktor — плагин HttpCookies сам подставит её в запросы
        val gatewayUrl = Url("https://${ApiConstants.GATEWAY_HOST}")
        HttpClientFactory.sessionCookieStorage.addCookie(
            gatewayUrl,
            Cookie(
                name     = "SESSION",
                value    = sessionValue,
                domain   = ApiConstants.GATEWAY_HOST,
                path     = "/",
                secure   = true,
                httpOnly = true
            )
        )
        log("syncSession ✓ SESSION сохранён: $sessionValue")

        // Проверяем что кука реально легла в хранилище
        val stored = HttpClientFactory.sessionCookieStorage
            .get(gatewayUrl)
            .find { it.name == "SESSION" }
        log("syncSession → Проверка хранилища: ${stored?.value ?: "НЕ НАЙДЕНО!"}")
    }

    override suspend fun getUserInfo(): Result<UserInfo> {
        return try {

            // Проверяем SESSION в хранилище (для диагностики — Ktor подставит его сам)
            val storedSession = HttpClientFactory.sessionCookieStorage
                .get(Url("https://${ApiConstants.GATEWAY_HOST}"))
                .find { it.name == "SESSION" }
                ?.value
            log("getUserInfo → SESSION в хранилище: $storedSession")

            if (storedSession.isNullOrBlank()) {
                log("getUserInfo ✗ SESSION отсутствует в хранилище!")
                return Result.failure(Exception("SESSION не найден — авторизация не завершена"))
            }

            // ШАГ 1: CSRF — НЕТ ручного Cookie-заголовка, плагин подставит сам
            log("getUserInfo → [1/2] GET ${ApiConstants.CSRF_ENDPOINT}")

            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }

            log("getUserInfo → CSRF статус: ${csrfResponse.status}")

            val csrfText = csrfResponse.bodyAsText()
            log("getUserInfo → CSRF тело: ${csrfText.take(200)}")

            if (csrfText.trim().startsWith("<")) {
                log("getUserInfo ✗ CSRF вернул HTML — сессия не принята шлюзом!")
                return Result.failure(Exception("CSRF вернул HTML"))
            }

            if (csrfResponse.status != HttpStatusCode.OK) {
                log("getUserInfo ✗ CSRF статус: ${csrfResponse.status}")
                return Result.failure(Exception("CSRF статус ${csrfResponse.status}"))
            }

            val csrfData = csrfResponse.body<CsrfResponse>()
            log("getUserInfo → CSRF: headerName=${csrfData.headerName}, token=${csrfData.token.take(20)}...")

            // ШАГ 2: ПРОФИЛЬ — НЕТ ручного Cookie-заголовка, плагин подставит сам
            log("getUserInfo → [2/2] GET ${ApiConstants.ACCOUNT_INFO}")

            val profileResponse = client.get(ApiConstants.ACCOUNT_INFO) {
                header(HttpHeaders.Accept,  "application/json")
                header(csrfData.headerName, csrfData.token) // "X-CSRF-TOKEN"
                header("X-Requested-With", "XMLHttpRequest")
            }

            log("getUserInfo → Профиль статус: ${profileResponse.status}")

            val profileText = profileResponse.bodyAsText()
            log("getUserInfo → Профиль тело (первые 300 символов): ${profileText.take(300)}")

            if (profileText.trim().startsWith("<")) {
                log("getUserInfo ✗ Профиль вернул HTML!")
                return Result.failure(Exception("account/info вернул HTML"))
            }

            if (profileResponse.status != HttpStatusCode.OK) {
                log("getUserInfo ✗ Неожиданный статус: ${profileResponse.status}")
                return Result.failure(Exception("account/info статус ${profileResponse.status}"))
            }

            val dto = profileResponse.body<UserInfoDto>()
            log("getUserInfo ✓ Успех! username=${dto.username}, roles=${dto.roles}")

            Result.success(
                UserInfo(
                    id       = dto.id,
                    username = dto.username,
                    fullName = dto.fullName,
                    roles    = dto.roles.map { mapRole(it) }
                )
            )

        } catch (e: Exception) {
            log("getUserInfo ✗ ИСКЛЮЧЕНИЕ: ${e::class.simpleName} — ${e.message}")
            Result.failure(e)
        }
    }

    private fun mapRole(raw: String): Role = when (raw.uppercase()) {
        "SUPER_ADMIN", "ROLE_SUPER_ADMIN" -> Role.SUPER_ADMIN
        "ADMIN",       "ROLE_ADMIN"       -> Role.ADMIN
        "TRACKER",     "ROLE_TRACKER"     -> Role.TRACKER
        else -> Role.UNKNOWN.also { log("mapRole → неизвестная роль: $raw") }
    }

    private fun log(message: String) {
        println("[$TAG] $message")
    }

    override suspend fun exchangeCodeForSession(code: String): Result<Unit> = Result.success(Unit)
    override suspend fun getCsrfToken(): Result<CsrfToken> = Result.failure(Exception("Not used directly"))
    override suspend fun login(username: String, password: String, csrf: CsrfToken): Result<Unit> = Result.success(Unit)
}