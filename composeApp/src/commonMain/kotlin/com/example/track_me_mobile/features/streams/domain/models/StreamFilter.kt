package com.example.track_me_mobile.features.streams.domain.models

data class StreamFilter(
    val fieldName: String,
    val type: String,   // "EQ", "LIKE"
    val value: String
)