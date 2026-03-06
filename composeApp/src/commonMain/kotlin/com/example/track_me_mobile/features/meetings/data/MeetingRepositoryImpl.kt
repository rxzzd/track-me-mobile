package com.example.track_me_mobile.features.meetings.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.meetings.data.models.MeetingPageDto
import com.example.track_me_mobile.features.meetings.data.models.MeetingUpdateRequestDto
import com.example.track_me_mobile.features.meetings.domain.MeetingRepository
import com.example.track_me_mobile.features.meetings.domain.models.Meeting
import com.example.track_me_mobile.features.meetings.domain.models.MeetingPage
import com.example.track_me_mobile.features.meetings.domain.models.MeetingUpdateRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class MeetingRepositoryImpl(
    private val client: HttpClient
) : MeetingRepository {

    override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> {
        return try {
            val response = client.get(ApiConstants.MEETINGS_LIST) {
                parameter("teamCardId", teamCardId)
                parameter("page", page)
                parameter("size", size)
                header(HttpHeaders.Accept, "application/json")
            }
            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("HTTP ${response.status}"))
            }
            val dto = response.body<MeetingPageDto>()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> {
        return try {
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            val csrfData = csrfResponse.body<CsrfResponse>()

            val body = MeetingUpdateRequestDto(
                id = request.id,
                link = request.link,
                number = request.number,
                teamStatus = request.teamStatus,
                tasksCurrentMeeting = request.tasksCurrentMeeting,
                tasksNextMeeting = request.tasksNextMeeting,
                startDate = request.startDate,
                status = request.status,
                teamCardId = request.teamCardId
            )

            // ДЕТАЛЬНОЕ ЛОГИРОВАНИЕ
            println("[MeetingRepo] ========== SENDING UPDATE REQUEST ==========")
            println("[MeetingRepo] URL: ${ApiConstants.updateMeeting(meetingId)}")
            println("[MeetingRepo] Method: PATCH")
            println("[MeetingRepo] Body:")
            println("[MeetingRepo]   id: ${body.id}")
            println("[MeetingRepo]   link: ${body.link}")
            println("[MeetingRepo]   number: ${body.number}")
            println("[MeetingRepo]   teamStatus: ${body.teamStatus}")
            println("[MeetingRepo]   tasksCurrentMeeting: ${body.tasksCurrentMeeting}")
            println("[MeetingRepo]   tasksNextMeeting: ${body.tasksNextMeeting}")
            println("[MeetingRepo]   startDate: ${body.startDate}")
            println("[MeetingRepo]   status: ${body.status}")
            println("[MeetingRepo]   teamCardId: ${body.teamCardId}")
            println("[MeetingRepo] ================================================")

            val response = client.patch(ApiConstants.updateMeeting(meetingId)) {
                parameter("teamCardId", teamCardId)
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(body)
            }

            println("[MeetingRepo] Response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                println("[MeetingRepo] Update successful!")
                Result.success(Unit)
            } else {
                // Пытаемся получить тело ошибки
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    "Could not read error body"
                }

                println("[MeetingRepo] ========== ERROR RESPONSE ==========")
                println("[MeetingRepo] Status: ${response.status}")
                println("[MeetingRepo] Error body: $errorBody")
                println("[MeetingRepo] =====================================")

                Result.failure(Exception("Ошибка: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            println("[MeetingRepo] Exception during update: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteMeeting(meetingId: String): Result<Unit> {
        return try {
            // Получаем CSRF токен
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT)
            val csrfData = csrfResponse.body<CsrfResponse>()

            // Выполняем DELETE запрос
            val response = client.delete(ApiConstants.deleteMeeting(meetingId)) {
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка удаления: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> {
        return try {
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT)
            val csrfData = csrfResponse.body<CsrfResponse>()

            val response = client.post(ApiConstants.meetingImage(meetingId)) {
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", fileBytes, Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=\"screen.png\"")
                            })
                        }
                    )
                )
            }
            if (response.status == HttpStatusCode.OK) Result.success(Unit)
            else Result.failure(Exception("Image upload failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun MeetingPageDto.toDomain() = MeetingPage(
        content = content.map { it.toDomain() },
        totalPages = page.totalPages,
        totalElements = page.totalElements,
        currentPage = page.number
    )

    private fun com.example.track_me_mobile.features.meetings.data.models.MeetingDto.toDomain() = Meeting(
        id = id,
        teamCardId = teamCardId ?: "",
        number = number ?: "1",
        link = link ?: "",
        startDate = startDate ?: "",
        teamStatus = teamStatus ?: "OK",
        status = status ?: "SCHEDULED",
        tasksCurrentMeeting = tasksCurrentMeeting ?: "",
        tasksNextMeeting = tasksNextMeeting ?: "",
        imageUrl = ApiConstants.meetingImage(id) // Формируем URL для загрузки картинки
    )

    override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> {
        return try {
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT)
            val csrfData = csrfResponse.body<com.example.track_me_mobile.features.auth.data.model.CsrfResponse>()

            val response = client.post(ApiConstants.CREATE_MEETING) {
                parameter("teamCardId", teamCardId)
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                // Отправляем пустую болванку, которую потом пользователь заполнит при редактировании
                setBody(MeetingUpdateRequestDto(
                    id = "",  // Пустой при создании
                    link = "",
                    number = number,
                    teamStatus = "OK",
                    tasksCurrentMeeting = "",
                    tasksNextMeeting = "",
                    startDate = startDateIso,
                    status = "SCHEDULED",
                    teamCardId = teamCardId
                ))
            }
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) Result.success(Unit)
            else Result.failure(Exception("Ошибка: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}