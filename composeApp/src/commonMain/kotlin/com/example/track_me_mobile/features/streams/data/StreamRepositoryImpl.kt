package com.example.track_me_mobile.features.streams.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.streams.data.model.NtiMarketDto
import com.example.track_me_mobile.features.streams.data.model.StreamCreateDto
import com.example.track_me_mobile.features.streams.data.model.StreamFilterDto
import com.example.track_me_mobile.features.streams.data.model.StreamFilterRequest
import com.example.track_me_mobile.features.streams.data.model.StreamPageDto
import com.example.track_me_mobile.features.streams.domain.StreamPage
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import com.example.track_me_mobile.features.streams.domain.models.Stream
import com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest
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
            println("### STREAM_DEBUG: Входящие фильтры: $filters")

            val body = StreamFilterRequest(
                filters = filters.map { f ->
                    StreamFilterDto(
                        fieldName = f.fieldName,
                        type = f.type,
                        values = listOf(f.value)
                    )
                }
            )

            if (body.filters.isEmpty()) {
                println("### STREAM_DEBUG WARNING: Список фильтров ПУСТ. Бэкенд может вернуть 400.")
            }

            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            val csrfData = csrfResponse.body<com.example.track_me_mobile.features.auth.data.model.CsrfResponse>()

            println("### STREAM_DEBUG → POST ${ApiConstants.STREAMS_ENDPOINT}?page=$page&size=$size")



            val response = client.post(ApiConstants.STREAMS_ENDPOINT) {
                parameter("page", page)
                parameter("size", size)
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Accept, "application/json")
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(body)
            }

            val rawBody = response.bodyAsText()
            println("### STREAM_DEBUG ← Status: ${response.status}")
            println("### STREAM_DEBUG ← Raw Body: $rawBody")

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("HTTP ${response.status}: $rawBody"))
            }

            val dto = response.body<StreamPageDto>()
            Result.success(dto.toDomain())

        } catch (e: Exception) {
            println("### STREAM_DEBUG CRASH: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getNtiMarkets(): Result<List<NtiMarket>> {
        return try {
            println("### NTI_DEBUG: Запрос рынков на ${ApiConstants.NTI_MARKETS_ENDPOINT}")

            val response = client.get(ApiConstants.NTI_MARKETS_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }

            println("### NTI_DEBUG: Status: ${response.status}")
            val rawBody = response.bodyAsText()
            println("### NTI_DEBUG: Body: $rawBody")

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("HTTP ${response.status}"))
            }

            val markets = response.body<List<NtiMarketDto>>()
            println("### NTI_DEBUG: Распарсено рынков: ${markets.size}")

            Result.success(markets.map { NtiMarket(it.id, it.name, it.displayName) })
        } catch (e: Exception) {
            println("### NTI_DEBUG ERROR: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

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

    override suspend fun createStream(request: StreamCreateRequest): Result<Stream> {
        return try {
            val streamDto = StreamCreateDto(
                name = request.name,
                startDate = request.startDate,
                endDate = request.endDate,
                ntiMarketIds = request.ntiMarketIds,
                description = request.description,
                trackStartDate = request.trackStartDate,
                meetingsCount = request.meetingsCount
            )

            println("### CREATE_PAYLOAD: $streamDto")

            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            val csrfData = csrfResponse.body<com.example.track_me_mobile.features.auth.data.model.CsrfResponse>()

            val response = client.post("https://api.trackme.test.startup-poligon.com/backend/api/v1/admin/stream") {
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(streamDto)
            }

            val rawBody = response.bodyAsText()
            println("### CREATE_RESPONSE Status: ${response.status}")
            println("### CREATE_RESPONSE Body: $rawBody")

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                val dto = response.body<com.example.track_me_mobile.features.streams.data.model.StreamDto>()
                Result.success(dto.toDomain())
            } else {
                // Если тут 400, в rawBody будет текст ошибки валидации
                Result.failure(Exception("Ошибка сервера: ${response.status}. $rawBody"))
            }
        } catch (e: Exception) {
            println("### CREATE_CRASH: ${e.message}")
            Result.failure(e)
        }
    }
}