package com.example.track_me_mobile.features.reports.data.model

import com.example.track_me_mobile.features.reports.domain.models.ReportItem
import kotlinx.serialization.Serializable

@Serializable
data class ReportItemDto(
    val streamName: String,
    val startDate: String,
    val endDate: String,
    val teamCardName: String,
    val username: String,
    val averageTeamGrade: Double?,  // Nullable - может быть null
    val averageUserGrade: Double?,  // Nullable - может быть null
    val meetingsCountPlan: Int,
    val meetingsCountFact: Int,
    val ntiMarkets: List<String>,
    val readinessLevel: String
) {
    fun toDomain() = ReportItem(
        streamName = streamName,
        startDate = startDate,
        endDate = endDate,
        teamCardName = teamCardName,
        username = username,
        averageTeamGrade = averageTeamGrade ?: 0.0,  // Дефолт 0.0
        averageUserGrade = averageUserGrade ?: 0.0,  // Дефолт 0.0
        meetingsCountPlan = meetingsCountPlan,
        meetingsCountFact = meetingsCountFact,
        ntiMarkets = ntiMarkets,
        readinessLevel = readinessLevel
    )
}

@Serializable
data class PageInfo(
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
)

@Serializable
data class ReportsPageDto(
    val content: List<ReportItemDto>,
    val page: PageInfo
)