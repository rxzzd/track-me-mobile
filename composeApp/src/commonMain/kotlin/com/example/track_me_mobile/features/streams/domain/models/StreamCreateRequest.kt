package com.example.track_me_mobile.features.streams.domain.models

data class StreamCreateRequest(
    val name: String,
    val startDate: String,
    val endDate: String,
    val ntiMarketIds: List<String>,
    val description: String = "",
    val trackStartDate: String,
    val meetingsCount: Int = 0
)