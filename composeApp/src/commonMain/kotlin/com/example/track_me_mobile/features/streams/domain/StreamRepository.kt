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
}

data class StreamPage(
    val content: List<Stream>,
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int
)