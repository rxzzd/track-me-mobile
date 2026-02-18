package com.example.track_me_mobile.features.streams.domain

import com.example.track_me_mobile.features.streams.domain.models.Stream
import com.example.track_me_mobile.features.streams.domain.models.StreamFilter

interface StreamRepository {
    // Возвращает страницу потоков. page — номер страницы (0-based), size — размер.
    suspend fun getStreams(
        filters: List<StreamFilter> = emptyList(),
        page: Int = 0,
        size: Int = 100
    ): Result<StreamPage>
}

data class StreamPage(
    val content: List<Stream>,
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int
)

