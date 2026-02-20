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
            val body = StreamFilterRequest(
                filters = filters.map { f ->
                    StreamFilterDto(
                        fieldName = f.fieldName,
                        type = f.type,
                        values = listOf(f.value)
                    )
                }
            )

            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            if (csrfResponse.status != HttpStatusCode.OK) {
                return Result.failure(Exception("CSRF вернул статус ${csrfResponse.status}"))
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

    override suspend fun getNtiMarkets(): Result<List<NtiMarket>> {
        return try {
            println("### NTI_MARKETS: Fetching from ${ApiConstants.NTI_MARKETS_ENDPOINT}")

            val response = client.get(ApiConstants.NTI_MARKETS_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("HTTP ${response.status}"))
            }

            val markets = response.body<List<NtiMarketDto>>()
            println("### NTI_MARKETS: Loaded ${markets.size} markets")

            Result.success(markets.map { NtiMarket(it.id, it.name, it.displayName) })
        } catch (e: Exception) {
            println("### NTI_MARKETS ERROR: ${e.message}")
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
            // 1. Получаем CSRF
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            val csrfData = csrfResponse.body<com.example.track_me_mobile.features.auth.data.model.CsrfResponse>()

            // 2. Отправляем POST
            val response = client.post(ApiConstants.CREATE_STREAM_ENDPOINT) {
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(
                    StreamCreateDto(
                        name = request.name,
                        startDate = request.startDate,
                        endDate = request.endDate,
                        ntiMarketIds = request.ntiMarketIds,
                        description = request.description,
                        trackStartDate = request.trackStartDate,
                        meetingsCount = request.meetingsCount
                    )
                )
            }

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                val dto = response.body<com.example.track_me_mobile.features.streams.data.model.StreamDto>()
                Result.success(dto.toDomain())
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Ошибка сервера: ${response.status}. $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}