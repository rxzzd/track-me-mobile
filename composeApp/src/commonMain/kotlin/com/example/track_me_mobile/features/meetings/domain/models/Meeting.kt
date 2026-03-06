package com.example.track_me_mobile.features.meetings.domain.models

data class Meeting(
    val id: String,
    val teamCardId: String,
    val number: String,
    val link: String,
    val startDate: String, // Храним оригинальную дату ISO, чтобы не потерять год/время
    val teamStatus: String, // "OK", "PROBLEMS", "CRITICAL" (по маппингу)
    val status: String, // "SCHEDULED", "COMPLETED", "CANCELLED"
    val tasksCurrentMeeting: String,
    val tasksNextMeeting: String,
    val imageUrl: String?
)

data class MeetingPage(
    val content: List<Meeting>,
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int
)

data class MeetingUpdateRequest(
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