package com.example.track_me_mobile.core.feedback.domain.models

data class FeedbackData(
    val designRating: Int,           // 1-5
    val usabilityRating: Int,        // 1-5
    val bugsCount: Int,
    val liked: String,
    val disliked: String,
    val suggestions: String,
    val username: String,
    val email: String,
    val fullName: String
)