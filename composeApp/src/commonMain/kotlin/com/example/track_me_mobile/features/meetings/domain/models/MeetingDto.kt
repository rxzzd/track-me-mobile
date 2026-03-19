package com.example.track_me_mobile.features.meetings.data.models

import kotlinx.serialization.Serializable

@Serializable
data class MeetingPageDto(
    val content: List<MeetingDto>,
    val page: PageMetaDto
)

@Serializable
data class MeetingDto(
    val id: String,
    val link: String? = null,
    val number: String? = null,
    val startDate: String? = null,
    val teamStatus: String? = null,
    val status: String? = null,
    val teamCardId: String? = null,
    val tasksCurrentMeeting: String? = null,
    val tasksNextMeeting: String? = null
)

@Serializable
data class PageMetaDto(
    val size: Int,
    val number: Int,
    val totalElements: Long,
    val totalPages: Int
)

@Serializable
data class MeetingUpdateRequestDto(
    val id: String,
    val link: String,
    val number: String,
    val teamStatus: String,
    val tasksCurrentMeeting: String,
    val tasksNextMeeting: String,
    val startDate: String,
    val status: String,
    val teamCardId: String
)