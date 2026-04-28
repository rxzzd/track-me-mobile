package com.example.track_me_mobile.features.reports.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.reports.domain.StreamMeetingReportRepository
import com.example.track_me_mobile.features.reports.domain.models.StreamMeetingReportItem
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class StreamMeetingReportItemDto(
    val streamId: String? = null,
    val streamName: String? = null,
    val teamId: String? = null,
    val teamName: String,
    val startDate: String? = null,
    val trackerName: String? = null,
    val trackerFullName: String? = null,
    val tasksNextMeeting: String? = null,
    val tasksCurrentMeeting: String? = null,
    val status: String? = null,
    val teamStatus: String? = null
) {
    fun toDomain() = StreamMeetingReportItem(
        streamId = streamId,
        streamName = streamName,
        teamId = teamId,
        teamName = teamName,
        startDate = startDate,
        trackerName = trackerName,
        trackerFullName = trackerFullName,
        tasksNextMeeting = tasksNextMeeting,
        tasksCurrentMeeting = tasksCurrentMeeting,
        status = status,
        teamStatus = teamStatus
    )
}

@Serializable
data class StreamMeetingReportsPageDto(
    val content: List<StreamMeetingReportItemDto>
)

class StreamMeetingReportRepositoryImpl(
    private val client: HttpClient
) : StreamMeetingReportRepository {

    override suspend fun getReportsByStream(
        streamId: String,
        page: Int,
        size: Int,
        sort: List<String>
    ): Result<List<StreamMeetingReportItem>> {
        return try {
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            val csrfData = csrfResponse.body<CsrfResponse>()

            val response = client.post(ApiConstants.MEETING_REPORTS) {
                parameter("streamId", streamId)
                parameter("page", page)
                parameter("size", size)
                sort.forEach { parameter("sort", it) }
                header(HttpHeaders.Accept, "application/json")
                header(csrfData.headerName, csrfData.token)
                contentType(ContentType.Application.Json)
                setBody(ReportRequestBody(filters = emptyList()))
            }

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Ошибка: ${response.status}"))
            }

            val dto = response.body<StreamMeetingReportsPageDto>()
            Result.success(dto.content.map { it.toDomain() })
        } catch (e: Exception) {
            println("[StreamMeetingReport] Ошибка: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun downloadReportsExcel(
        streamId: String,
        trackerFilter: String?,
        teamFilter: String?,
        statusFilter: String?,
        page: Int,
        size: Int,
        sort: List<String>
    ): Result<ByteArray> {
        return try {
            println("[StreamMeetingReport] Выгрузка Excel: streamId=$streamId, tracker=$trackerFilter, team=$teamFilter, status=$statusFilter")

            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            val csrfData = csrfResponse.body<CsrfResponse>()

            val filters = mutableListOf<ReportFilterDto>()

            if (trackerFilter != null && trackerFilter != "Все") {
                filters.add(
                    ReportFilterDto(
                        fieldName = "trackerFullName",
                        type = "EQ",
                        values = listOf(trackerFilter)
                    )
                )
            }

            if (teamFilter != null && teamFilter != "Все") {
                filters.add(
                    ReportFilterDto(
                        fieldName = "teamName",
                        type = "EQ",
                        values = listOf(teamFilter)
                    )
                )
            }

            if (statusFilter != null && statusFilter != "Все") {
                filters.add(
                    ReportFilterDto(
                        fieldName = "teamStatus",
                        type = "EQ",
                        values = listOf(statusFilter)
                    )
                )
            }

            val requestBody = ReportRequestBody(filters = filters)
            val response = client.post("${ApiConstants.MEETING_REPORTS}/excel") {
                parameter("streamId", streamId)
                parameter("page", page)
                parameter("size", size)
                sort.forEach { parameter("sort", it) }
                header(HttpHeaders.Accept, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                header(csrfData.headerName, csrfData.token)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Ошибка: ${response.status}"))
            }

            val bytes = response.body<ByteArray>()
            Result.success(bytes)
        } catch (e: Exception) {
            println("[StreamMeetingReport] Excel выгрузка ошибка: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
