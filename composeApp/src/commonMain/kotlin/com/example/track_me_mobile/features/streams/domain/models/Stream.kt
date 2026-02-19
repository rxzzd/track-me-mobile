package com.example.track_me_mobile.features.streams.domain.models

data class Stream(
    val id: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val description: String,
    val active: Boolean,
    val trackStartDate: String,
    val meetingsCount: Int,
    val ntiMarkets: List<NtiMarket>
)

data class NtiMarket(
    val id: String,
    val name: String,
    val displayName: String
)