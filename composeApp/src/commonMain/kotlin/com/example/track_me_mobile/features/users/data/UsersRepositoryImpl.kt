package com.example.track_me_mobile.features.users.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.users.data.model.*
import com.example.track_me_mobile.features.users.domain.UsersRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*

class UsersRepositoryImpl(
    private val httpClient: HttpClient
) : UsersRepository {

    override suspend fun getTrackers(
        page: Int,
        size: Int,
        sort: List<String>,
        showBlocked: Boolean
    ): Result<PagedResponse<UserDto>> {
        return try {
            println("USERS_REPO: Получение CSRF токена для trackers")
            val csrfData = getCsrfToken()

            // Сервер требует хотя бы один фильтр
            val requestBody = UsersRequest(
                filters = listOf(
                    FilterCriteria(
                        fieldName = "accountNonLocked",
                        type = "EQ",
                        values = emptyList(),
                        value = if (showBlocked) "false" else "true"
                    )
                )
            )

            println("USERS_REPO: POST ${ApiConstants.USERS_TRACKERS}")

            val response = httpClient.post(ApiConstants.USERS_TRACKERS) {
                url {
                    parameters.append("page", page.toString())
                    parameters.append("size", size.toString())
                    sort.forEach { parameters.append("sort", it) }
                }
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(requestBody)
            }

            println("USERS_REPO: Response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                val data = response.body<PagedResponse<UserDto>>()
                Result.success(data)
            } else {
                val errorBody = response.bodyAsText()
                println("USERS_REPO: Error response body: $errorBody")
                Result.failure(Exception("Сервер вернул статус ${response.status}: $errorBody"))
            }
        } catch (e: Exception) {
            println("USERS_REPO: Ошибка: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAdministrators(
        page: Int,
        size: Int,
        sort: List<String>,
        showBlocked: Boolean
    ): Result<PagedResponse<UserDto>> {
        return try {
            println("USERS_REPO: Получение CSRF токена для administrators")
            val csrfData = getCsrfToken()

            // Сервер требует хотя бы один фильтр
            val requestBody = UsersRequest(
                filters = listOf(
                    FilterCriteria(
                        fieldName = "accountNonLocked",
                        type = "EQ",
                        values = emptyList(),
                        value = if (showBlocked) "false" else "true"
                    )
                )
            )

            println("USERS_REPO: POST ${ApiConstants.USERS_ADMINISTRATORS}")

            val response = httpClient.post(ApiConstants.USERS_ADMINISTRATORS) {
                url {
                    parameters.append("page", page.toString())
                    parameters.append("size", size.toString())
                    sort.forEach { parameters.append("sort", it) }
                }
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(requestBody)
            }

            println("USERS_REPO: Response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                val data = response.body<PagedResponse<UserDto>>()
                Result.success(data)
            } else {
                val errorBody = response.bodyAsText()
                println("USERS_REPO: Error response body: $errorBody")
                Result.failure(Exception("Сервер вернул статус ${response.status}: $errorBody"))
            }
        } catch (e: Exception) {
            println("USERS_REPO: Ошибка: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getUserInfo(username: String): Result<UserDto> {
        return try {
            val url = "${ApiConstants.USERS_INFO}/$username/info"
            println("USERS_REPO: GET $url")

            val response = httpClient.get(url)

            println("USERS_REPO: getUserInfo response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                val data = response.body<UserDto>()
                println("USERS_REPO: getUserInfo успех - загружен профиль: ${data.fullName}")
                Result.success(data)
            } else {
                val errorBody = response.bodyAsText()
                println("USERS_REPO: getUserInfo ошибка: $errorBody")
                Result.failure(Exception("Сервер вернул статус ${response.status}: $errorBody"))
            }
        } catch (e: Exception) {
            println("USERS_REPO: getUserInfo exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun enableUser(username: String): Result<Unit> {
        return try {
            println("USERS_REPO: Включение пользователя: $username")
            val csrfData = getCsrfToken()

            val response = httpClient.post(ApiConstants.USERS_ENABLE) {
                url {
                    parameters.append("username", username)
                }
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
            }

            println("USERS_REPO: Enable response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Не удалось включить пользователя"))
            }
        } catch (e: Exception) {
            println("USERS_REPO: Ошибка enable: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun disableUser(username: String): Result<Unit> {
        return try {
            println("USERS_REPO: Отключение пользователя: $username")
            val csrfData = getCsrfToken()

            val response = httpClient.post(ApiConstants.USERS_DISABLE) {
                url {
                    parameters.append("username", username)
                }
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
            }

            println("USERS_REPO: Disable response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Не удалось отключить пользователя"))
            }
        } catch (e: Exception) {
            println("USERS_REPO: Ошибка disable: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun getCsrfToken(): CsrfResponse {
        val csrfResponse = httpClient.get(ApiConstants.CSRF_ENDPOINT) {
            header(HttpHeaders.Accept, "application/json")
        }

        if (csrfResponse.status != HttpStatusCode.OK) {
            throw Exception("Не удалось получить CSRF токен")
        }

        return csrfResponse.body()
    }
}