package com.example.track_me_mobile.features.reports.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.reports.data.model.ReportsPageDto
import com.example.track_me_mobile.features.reports.domain.ReportRepository
import com.example.track_me_mobile.features.reports.domain.models.ReportItem
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ReportFilterDto(
    val fieldName: String,
    val type: String,
    val values: List<String>
)

@Serializable
data class ReportRequestBody(
    val filters: List<ReportFilterDto>
)

class ReportRepositoryImpl(
    private val client: HttpClient
) : ReportRepository {

    override suspend fun getReports(
        trackerUsername: String?,
        streamName: String?,
        page: Int,
        size: Int,
        showInactive: Boolean
    ): Result<List<ReportItem>> {
        return try {
            println("[REPORTS] Загрузка отчётов: tracker=$trackerUsername, stream=$streamName, showInactive=$showInactive")

            // Получаем CSRF токен
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            val csrfData = csrfResponse.body<CsrfResponse>()

            // Формируем фильтры
            val filters = mutableListOf<ReportFilterDto>()

            if (trackerUsername != null && trackerUsername != "Все") {
                filters.add(ReportFilterDto(
                    fieldName = "username",
                    type = "EQ",
                    values = listOf(trackerUsername)
                ))
            }

            // ИСПРАВЛЕНИЕ: используем streams.name (вложенное поле)
            if (streamName != null && streamName != "Все") {
                filters.add(ReportFilterDto(
                    fieldName = "streams.name",
                    type = "EQ",
                    values = listOf(streamName)
                ))
            }

            if (!showInactive) {
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .toString()

                filters.add(ReportFilterDto(
                    fieldName = "streams.startDate",
                    type = "LTE",
                    values = listOf(today)
                ))
                filters.add(ReportFilterDto(
                    fieldName = "streams.endDate",
                    type = "GTE",
                    values = listOf(today)
                ))
            }

            val requestBody = ReportRequestBody(filters = filters)

            val endpoint = "${ApiConstants.BACKEND_BASE}/api/v1/team-cards/reports?page=$page&size=$size"

            println("[REPORTS] Запрос: $endpoint")
            println("[REPORTS] Body: ${Json.encodeToString(ReportRequestBody.serializer(), requestBody)}")

            val response = client.post(endpoint) {
                header(HttpHeaders.Accept, "application/json")
                header(csrfData.headerName, csrfData.token)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            println("[REPORTS] Status: ${response.status}")

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Ошибка: ${response.status}"))
            }

            val dto = response.body<ReportsPageDto>()
            val reports = dto.content.map { it.toDomain() }

            println("[REPORTS] Загружено отчётов: ${reports.size}")
            Result.success(reports)

        } catch (e: Exception) {
            println("[REPORTS] Ошибка: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun downloadReportsExcel(
        trackerUsername: String?,
        streamName: String?,
        page: Int,
        size: Int,
        showInactive: Boolean
    ): Result<ByteArray> {
        return try {
            println("[REPORTS] Выгрузка Excel: tracker=$trackerUsername, stream=$streamName, showInactive=$showInactive")

            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            val csrfData = csrfResponse.body<CsrfResponse>()

            val filters = mutableListOf<ReportFilterDto>()

            if (trackerUsername != null && trackerUsername != "Все") {
                filters.add(
                    ReportFilterDto(
                        fieldName = "username",
                        type = "EQ",
                        values = listOf(trackerUsername)
                    )
                )
            }

            if (streamName != null && streamName != "Все") {
                filters.add(
                    ReportFilterDto(
                        fieldName = "streams.name",
                        type = "EQ",
                        values = listOf(streamName)
                    )
                )
            }

            if (!showInactive) {
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .toString()

                filters.add(
                    ReportFilterDto(
                        fieldName = "streams.startDate",
                        type = "LTE",
                        values = listOf(today)
                    )
                )
                filters.add(
                    ReportFilterDto(
                        fieldName = "streams.endDate",
                        type = "GTE",
                        values = listOf(today)
                    )
                )
            }

            val requestBody = ReportRequestBody(filters = filters)
            val endpoint = "${ApiConstants.BACKEND_BASE}/api/v1/team-cards/reports/excel?page=$page&size=$size"

            val response = client.post(endpoint) {
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
            println("[REPORTS] Excel выгрузка ошибка: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
