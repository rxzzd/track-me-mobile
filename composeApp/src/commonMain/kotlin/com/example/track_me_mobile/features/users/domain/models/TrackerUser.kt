package com.example.track_me_mobile.features.tracker_list.domain.models

data class TrackerUser(
    val id: String,
    val fullName: String,
    val telegramNick: String,
    val isConfirmed: Boolean
)