package com.example.track_me_mobile.features.streams.data

import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.streams.data.model.*
import com.example.track_me_mobile.features.streams.domain.StreamPage
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.*
import com.example.track_me_mobile.features.teams.data.model.TeamCardDto
import com.example.track_me_mobile.features.teams.data.model.TeamCardsPageDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.request.forms.*


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

    override suspend fun getTeamsByStream(streamId: String): Result<List<com.example.track_me_mobile.features.teams.domain.models.TeamCard>> {
        return try {
            val endpoint = "${ApiConstants.BACKEND_BASE}/api/v1/admin/team-cards"
            println("### TEAMS_BY_STREAM -> URL: $endpoint")
            println("### TEAMS_BY_STREAM -> Looking for streamId: $streamId")

            val csrfData = getCsrf()

            val response = client.post(endpoint) {
                parameter("page", 0)
                parameter("size", 100) // Получаем больше команд
                contentType(ContentType.Application.Json)
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")
                setBody(mapOf("filters" to emptyList<Any>()))
            }

            println("### TEAMS_BY_STREAM <- Status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                val dto = response.body<TeamCardsPageDto>()
                println("### TEAMS_BY_STREAM <- Total teams: ${dto.content.size}")

                // Детальное логирование для отладки
                dto.content.forEachIndexed { index, team ->
                    println("### TEAMS_BY_STREAM Team[$index]: name=${team.name}, streams=${team.streams.map { it.id }}")
                }

                // Фильтруем команды, которые принадлежат данному потоку
                val teamsInStream = dto.content
                    .filter { team ->
                        val hasStream = team.streams.any { stream -> stream.id == streamId }
                        println("### TEAMS_BY_STREAM Team '${team.name}' has streamId=$streamId? $hasStream")
                        hasStream
                    }
                    .map { it.toDomainTeamCard() }

                println("### TEAMS_BY_STREAM <- Teams in stream $streamId: ${teamsInStream.size}")

                // Если не нашли команды через фильтрацию, всё равно возвращаем пустой список
                // (диалог покажет общее сообщение)
                Result.success(teamsInStream)
            } else {
                Result.failure(Exception("Failed to fetch teams"))
            }
        } catch (e: Exception) {
            println("### TEAMS_BY_STREAM ERROR: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
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

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    Result.success(Unit)
                }
                HttpStatusCode.BadRequest, HttpStatusCode.Conflict -> {
                    // 400 или 409 - проверяем сообщение об ошибке
                    val errorBody = response.bodyAsText()
                    println("### API_LOG (DELETE) ERROR BODY: $errorBody")

                    // Проверяем есть ли foreign key constraint в ошибке
                    if (errorBody.contains("foreign key constraint", ignoreCase = true) ||
                        errorBody.contains("fk_streams_team_cards_stream", ignoreCase = true) ||
                        errorBody.contains("stream_team_card", ignoreCase = true)) {
                        Result.failure(StreamHasTeamsException("Невозможно удалить поток: в потоке есть команды"))
                    } else {
                        Result.failure(Exception("Delete failed: ${response.status}"))
                    }
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    println("### API_LOG (DELETE) ERROR BODY: $errorBody")
                    Result.failure(Exception("Delete failed: ${response.status}"))
                }
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

    override suspend fun uploadStreamImage(streamId: String, imageBytes: ByteArray): Result<Unit> {
        return try {
            println("[StreamRepo] Uploading image for stream $streamId, size: ${imageBytes.size} bytes")

            // ДОБАВИТЬ: Получаем CSRF токен
            val csrfData = getCsrf()

            val response = client.post(ApiConstants.streamImage(streamId)) {
                // ДОБАВИТЬ: Передаём CSRF токен в header
                header(csrfData.headerName, csrfData.token)
                header("X-Requested-With", "XMLHttpRequest")

                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", imageBytes, Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"stream.jpg\"")
                            })
                        }
                    )
                )
            }

            if (response.status == HttpStatusCode.OK) {
                println("[StreamRepo] Image uploaded successfully")
                Result.success(Unit)
            } else {
                val error = response.bodyAsText()
                println("[StreamRepo] Upload failed: ${response.status}, $error")
                Result.failure(Exception("Ошибка загрузки: ${response.status}"))
            }
        } catch (e: Exception) {
            println("[StreamRepo] Upload error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getStreamImage(streamId: String): Result<ByteArray?> {
        return try {
            println("[StreamRepo] Fetching image for stream $streamId")

            val response = client.get(ApiConstants.streamImage(streamId))

            when (response.status) {
                HttpStatusCode.OK -> {
                    val bytes = response.readBytes()
                    println("[StreamRepo] Image loaded, size: ${bytes.size} bytes")
                    Result.success(bytes)
                }
                HttpStatusCode.NotFound -> {
                    println("[StreamRepo] No image found for stream")
                    Result.success(null)
                }
                else -> {
                    println("[StreamRepo] Failed to load image: ${response.status}")
                    Result.failure(Exception("Не удалось загрузить фото"))
                }
            }
        } catch (e: Exception) {
            println("[StreamRepo] Error loading image: ${e.message}")
            Result.failure(e)
        }
    }
    private suspend fun getCsrf(): com.example.track_me_mobile.features.auth.data.model.CsrfResponse {
        return client.get(ApiConstants.CSRF_ENDPOINT).body()
    }

    private fun StreamDto.toDomain() = Stream(id, name, startDate, endDate, description, active, trackStartDate, meetingsCount, ntiMarkets.map { NtiMarket(it.id, it.name, it.displayName) })
    private fun StreamPageDto.toDomain() = StreamPage(content.map { it.toDomain() }, page.totalPages, page.totalElements, page.number)
    private fun StreamCreateRequest.toDto() = StreamCreateDto(name, startDate, endDate, ntiMarketIds, description, trackStartDate, meetingsCount)

    private fun TeamCardDto.toDomainTeamCard() = com.example.track_me_mobile.features.teams.domain.models.TeamCard(
        id = id,
        name = name,
        description = description,
        status = status,
        username = username,
        enabled = enabled,
        ntiMarkets = ntiMarkets.map { com.example.track_me_mobile.features.teams.domain.models.NtiMarket(it.id, it.name, it.displayName) },
        readinessLevel = readinessLevel,
        averageGrade = averageGrade,
        stream = streams.firstOrNull()?.let {
            com.example.track_me_mobile.features.teams.domain.models.Stream(
                id = it.id,
                name = it.name,
                description = it.description,
                active = it.active,
                startDate = it.startDate,
                endDate = it.endDate
            )
        },
        meetingsCount = meetingsCount,
        meetingsCompletedCount = meetingsCompletedCount,
        meetingsNotHappenedCount = meetingsNotHappenedCount
    )
}

// Custom exception для случая когда поток содержит команды
class StreamHasTeamsException(message: String) : Exception(message)