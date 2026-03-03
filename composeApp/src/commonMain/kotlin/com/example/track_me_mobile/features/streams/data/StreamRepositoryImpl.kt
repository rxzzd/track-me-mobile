package com.example.track_me_mobile.features.streams.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.streams.data.model.*
import com.example.track_me_mobile.features.streams.domain.StreamPage
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class StreamRepositoryImpl(
    private val client: HttpClient
) : StreamRepository {

    override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> {
        return try {
            val body = StreamFilterRequest(
                filters = filters.map { f -> StreamFilterDto(f.fieldName, f.type, listOf(f.value)) }
            )
            val csrfData = getCsrf()
            val response = client.post(ApiConstants.STREAMS_ENDPOINT) {
                parameter("page", page)
                parameter("size", size)
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                setBody(body)
            }
            if (response.status == HttpStatusCode.OK) Result.success(response.body<StreamPageDto>().toDomain())
            else Result.failure(Exception("Error ${response.status}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getNtiMarkets(): Result<List<NtiMarket>> {
        return try {
            val response = client.get(ApiConstants.NTI_MARKETS_ENDPOINT)
            if (response.status == HttpStatusCode.OK) {
                val markets = response.body<List<NtiMarketDto>>()
                Result.success(markets.map { NtiMarket(it.id, it.name, it.displayName) })
            } else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getStream(id: String): Result<Stream> {
        return try {
            val response = client.get("${ApiConstants.BACKEND_BASE}/api/v1/admin/stream/$id")
            if (response.status == HttpStatusCode.OK) Result.success(response.body<StreamDto>().toDomain())
            else Result.failure(Exception("Error"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteStream(streamId: String): Result<Unit> {
        return try {
            val csrfData = getCsrf()
            val endpoint = "${ApiConstants.BACKEND_BASE}/api/v1/admin/stream/$streamId"

            println("### API_LOG (DELETE) -> URL: $endpoint")

            val response = client.delete(endpoint) {
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
            }

            println("### API_LOG (DELETE) <- Status: ${response.status}")

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NoContent) {
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                println("### API_LOG (DELETE) ERROR BODY: $errorBody")
                Result.failure(Exception("Delete failed: ${response.status}"))
            }
        } catch (e: Exception) {
            println("### API_LOG (DELETE) CRASH: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun createStream(request: StreamCreateRequest): Result<Stream> {
        return try {
            val dto = request.toDto()
            val csrfData = getCsrf()

            println("### API_LOG (CREATE) -> Body: $dto")

            val response = client.post("${ApiConstants.BACKEND_BASE}/api/v1/admin/stream") {
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                setBody(dto)
            }

            println("### API_LOG (CREATE) <- Status: ${response.status}")

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                Result.success(response.body<StreamDto>().toDomain())
            } else {
                val errorBody = response.bodyAsText()
                println("### API_LOG (CREATE) ERROR BODY: $errorBody")
                Result.failure(Exception("Create failed: ${response.status}"))
            }
        } catch (e: Exception) {
            println("### API_LOG (CREATE) CRASH: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateStream(id: String, request: StreamCreateRequest): Result<Stream> {
        return try {
            val dto = request.toDto()
            val csrfData = getCsrf()
            val endpoint = "${ApiConstants.BACKEND_BASE}/api/v1/admin/stream/$id"

            println("### API_LOG (UPDATE) -> URL: $endpoint")
            println("### API_LOG (UPDATE) -> Body: $dto")

            val response = client.patch(endpoint) {
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(dto)
            }

            println("### API_LOG (UPDATE) <- Status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body<StreamDto>().toDomain())
            } else {
                val errorBody = response.bodyAsText()
                println("### API_LOG (UPDATE) ERROR BODY: $errorBody")
                Result.failure(Exception("Update failed: ${response.status}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            println("### API_LOG (UPDATE) CRASH: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.success(Unit)

    private suspend fun getCsrf(): com.example.track_me_mobile.features.auth.data.model.CsrfResponse {
        return client.get(ApiConstants.CSRF_ENDPOINT).body()
    }

    private fun StreamDto.toDomain() = Stream(id, name, startDate, endDate, description, active, trackStartDate, meetingsCount, ntiMarkets.map { NtiMarket(it.id, it.name, it.displayName) })
    private fun StreamPageDto.toDomain() = StreamPage(content.map { it.toDomain() }, page.totalPages, page.totalElements, page.number)
    private fun StreamCreateRequest.toDto() = StreamCreateDto(name, startDate, endDate, ntiMarketIds, description, trackStartDate, meetingsCount)
}