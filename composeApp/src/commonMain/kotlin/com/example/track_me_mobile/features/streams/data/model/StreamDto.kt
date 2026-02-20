package com.example.track_me_mobile.features.streams.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StreamPageDto(
    val content: List<StreamDto>,
    val page: PageMetaDto
)

@Serializable
data class StreamDto(
    val id: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val description: String,
    val active: Boolean,
    val trackStartDate: String,
    val meetingsCount: Int,
    val ntiMarkets: List<NtiMarketDto>
)

@Serializable
data class NtiMarketDto(
    val id: String,
    val name: String,
    val displayName: String
)

@Serializable
data class PageMetaDto(
    val size: Int,
    val number: Int,
    val totalElements: Long,
    val totalPages: Int
)

@Serializable
data class StreamCreateDto(
    val name: String,
    val startDate: String,
    val endDate: String,
    val ntiMarketIds: List<String>,
    val description: String,
    val trackStartDate: String,
    val meetingsCount: Int
)