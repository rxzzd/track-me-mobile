package com.example.track_me_mobile.features.streams.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.streams.data.model.StreamFilterDto
import com.example.track_me_mobile.features.streams.data.model.StreamFilterRequest
import com.example.track_me_mobile.features.streams.data.model.StreamPageDto
import com.example.track_me_mobile.features.streams.domain.StreamPage
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import com.example.track_me_mobile.features.streams.domain.models.Stream
import com.example.track_me_mobile.features.streams.domain.models.StreamFilter
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class StreamRepositoryImpl(
    private val client: HttpClient
) : StreamRepository {

    override suspend fun getStreams(
        filters: List<StreamFilter>,
        page: Int,
        size: Int
    ): Result<StreamPage> {
        return try {
            val body = StreamFilterRequest(
                filters = filters.map { f ->
                    StreamFilterDto(
                        fieldName = f.fieldName,
                        type = f.type,
                        values = listOf(f.value)
                    )
                }
            )

            // ШАГ 1: получаем CSRF токен (SESSION подставляется плагином автоматически)
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            if (csrfResponse.status != HttpStatusCode.OK) {
                return Result.failure(Exception("CSRF вернул статус ${csrfResponse.status}"))
            }
            val csrfData = csrfResponse.body<com.example.track_me_mobile.features.auth.data.model.CsrfResponse>()

            println("### STREAM_DEBUG → POST ${ApiConstants.STREAMS_ENDPOINT}?page=$page&size=$size")

            // ШАГ 2: POST с CSRF токеном в заголовке
            val response = client.post(ApiConstants.STREAMS_ENDPOINT) {
                parameter("page", page)
                parameter("size", size)
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Accept, "application/json")
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(body)
            }

            println("### STREAM_DEBUG ← status: ${response.status}")

            val rawBody = response.bodyAsText()
            println("### STREAM_DEBUG ← body: $rawBody")

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("HTTP ${response.status}: $rawBody"))
            }

            val dto = response.body<StreamPageDto>()
            Result.success(dto.toDomain())

        } catch (e: Exception) {
            println("### CRASH: ${e::class.simpleName}: ${e.message}")
            println("### CRASH stacktrace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    // Маппинг DTO → Domain
    private fun StreamPageDto.toDomain() = StreamPage(
        content = content.map { it.toDomain() },
        totalPages = page.totalPages,
        totalElements = page.totalElements,
        currentPage = page.number
    )

    private fun com.example.track_me_mobile.features.streams.data.model.StreamDto.toDomain() = Stream(
        id = id,
        name = name,
        startDate = startDate,
        endDate = endDate,
        description = description,
        active = active,
        trackStartDate = trackStartDate,
        meetingsCount = meetingsCount,
        ntiMarkets = ntiMarkets.map { NtiMarket(it.id, it.name, it.displayName) }
    )

}
