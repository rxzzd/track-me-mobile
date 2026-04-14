package com.example.track_me_mobile.features.reports.domain.models

data class ReportItem(
    val streamId: String? = null,
    val streamName: String,
    val startDate: String,
    val endDate: String,
    val teamId: String? = null,
    val teamCardName: String,
    val username: String,
    val averageTeamGrade: Double,
    val averageUserGrade: Double,
    val meetingsCountPlan: Int,
    val meetingsCountFact: Int,
    val ntiMarkets: List<String>,
    val readinessLevel: String
)