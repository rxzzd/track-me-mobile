package com.example.track_me_mobile.features.streams.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StreamFilterRequest(
    val filters: List<StreamFilterDto>
)

@Serializable
data class StreamFilterDto(
    val fieldName: String,
    val type: String,
    val values: List<String>
)