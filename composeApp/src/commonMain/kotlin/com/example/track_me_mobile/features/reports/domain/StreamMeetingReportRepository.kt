package com.example.track_me_mobile.features.reports.domain

import com.example.track_me_mobile.features.reports.domain.models.StreamMeetingReportItem

interface StreamMeetingReportRepository {
    suspend fun getReportsByStream(
        streamId: String,
        page: Int = 0,
        size: Int = 10000,
        sort: List<String> = listOf("teamName,asc", "startDate,desc")
    ): Result<List<StreamMeetingReportItem>>
}
