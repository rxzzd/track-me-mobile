package com.example.track_me_mobile.core.feedback.domain

import com.example.track_me_mobile.core.feedback.domain.models.FeedbackData

interface FeedbackRepository {
    suspend fun sendFeedback(feedback: FeedbackData): Result<Unit>
}