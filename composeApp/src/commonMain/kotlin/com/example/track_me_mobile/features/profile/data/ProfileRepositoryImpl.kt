package com.example.track_me_mobile.features.profile.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.profile.domain.ProfileRepository
import com.example.track_me_mobile.features.profile.domain.models.UserProfile
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.statement.*

class ProfileRepositoryImpl(
    private val httpClient: HttpClient
) : ProfileRepository {

    override suspend fun getAccountInfo(): Result<UserProfile> {
        return try {
            val dto = httpClient.get(ApiConstants.ACCOUNT_INFO).body<AccountInfoDto>()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAccount(
        fullName: String,
        email: String,
        phoneNumber: String,
        avatarUrl: String?
    ): Result<Unit> {
        return try {
            println("PROFILE_REPO: Получение CSRF токена")

            // ШАГ 1: Получаем CSRF токен
            val csrfResponse = httpClient.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }

            if (csrfResponse.status != HttpStatusCode.OK) {
                println("PROFILE_REPO: CSRF вернул статус ${csrfResponse.status}")
                return Result.failure(Exception("Не удалось получить CSRF токен"))
            }

            val csrfData = csrfResponse.body<CsrfResponse>()
            println("PROFILE_REPO: CSRF токен получен: ${csrfData.headerName}")

            // ШАГ 2: Отправляем обновление с CSRF токеном
            println("PROFILE_REPO: POST ${ApiConstants.ACCOUNT_UPDATE}")
            println("PROFILE_REPO: Body: fullName=$fullName, email=$email, phone=$phoneNumber")

            val response = httpClient.post(ApiConstants.ACCOUNT_UPDATE) {
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(UpdateAccountRequest(fullName, email, phoneNumber, avatarUrl))
            }

            println("PROFILE_REPO: Response status: ${response.status}")


            val responseBody = response.bodyAsText()
            println("PROFILE_REPO: Response body: $responseBody")

            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Сервер вернул статус ${response.status}: $responseBody"))
            }
        } catch (e: Exception) {
            println("PROFILE_REPO: Ошибка: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun AccountInfoDto.toDomain() = UserProfile(
        id          = id,
        username    = username,
        fullName    = fullName,
        email       = email,
        phoneNumber = phoneNumber,
        avatarUrl   = avatarUrl,
        roles       = roles,
        enabled     = enabled
    )
}