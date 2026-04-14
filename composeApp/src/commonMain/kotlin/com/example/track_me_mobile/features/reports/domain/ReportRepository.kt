package com.example.track_me_mobile.features.reports.domain

import com.example.track_me_mobile.features.reports.domain.models.ReportItem

interface ReportRepository {
    suspend fun getReports(
        trackerUsername: String? = null,
        streamName: String? = null,
        page: Int = 0,
        size: Int = 100,
        showInactive: Boolean = false
    ): Result<List<ReportItem>>
}