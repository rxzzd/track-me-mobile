package com.example.track_me_mobile.features.reports.domain.models

data class StreamMeetingReportItem(
    val streamId: String? = null,
    val streamName: String? = null,
    val teamId: String? = null,
    val teamName: String,
    val startDate: String? = null,
    val trackerName: String? = null,
    val trackerFullName: String? = null,
    val tasksNextMeeting: String? = null,
    val tasksCurrentMeeting: String? = null,
    val status: String? = null,
    val teamStatus: String? = null
)
