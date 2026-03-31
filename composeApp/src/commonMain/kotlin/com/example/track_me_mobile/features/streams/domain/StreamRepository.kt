package com.example.track_me_mobile.features.streams.domain

import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import com.example.track_me_mobile.features.streams.domain.models.Stream
import com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest
import com.example.track_me_mobile.features.streams.domain.models.StreamFilter

interface StreamRepository {
    suspend fun getStreams(
        filters: List<StreamFilter> = emptyList(),
        page: Int = 0,
        size: Int = 100
    ): Result<StreamPage>

    suspend fun getNtiMarkets(): Result<List<NtiMarket>>

    suspend fun createStream(request: StreamCreateRequest): Result<Stream>

    suspend fun getStream(id: String): Result<Stream>

    suspend fun updateStream(id: String, request: StreamCreateRequest): Result<Stream>

    suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit>

    suspend fun getStreamImage(streamId: String): Result<ByteArray?>

    suspend fun deleteStream(streamId: String): Result<Unit>

    suspend fun getTeamsByStream(streamId: String): Result<List<com.example.track_me_mobile.features.teams.domain.models.TeamCard>>
}

data class StreamPage(
    val content: List<Stream>,
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int
)