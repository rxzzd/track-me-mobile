package com.example.track_me_mobile.core.feedback.data

import com.example.track_me_mobile.core.feedback.domain.FeedbackRepository
import com.example.track_me_mobile.core.feedback.domain.models.FeedbackData
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class FeedbackRepositoryImpl(
    private val client: HttpClient
) : FeedbackRepository {

    private val formspreeUrl = "https://formspree.io/f/xblyydjj"

    override suspend fun sendFeedback(feedback: FeedbackData): Result<Unit> {
        return try {
            println("[FEEDBACK] Отправка отзыва на Formspree")

            val response = client.submitForm(
                url = formspreeUrl,
                formParameters = parameters {
                    append("Дизайн сервиса", feedback.designRating.toString())
                    append("Удобство использования", feedback.usabilityRating.toString())
                    append("Количество багов", feedback.bugsCount.toString())
                    append("Что понравилось", feedback.liked)
                    append("Что не понравилось", feedback.disliked)
                    append("Рекомендации", feedback.suggestions)
                    append("Username", feedback.username)
                    append("Почта", feedback.email)
                    append("ФИО", feedback.fullName)
                }
            ) {
                header(HttpHeaders.Accept, "application/json")
            }

            if (response.status.isSuccess()) {
                println("[FEEDBACK] Отзыв успешно отправлен")
                Result.success(Unit)
            } else {
                println("[FEEDBACK] Ошибка: ${response.status}")
                Result.failure(Exception("Ошибка отправки: ${response.status}"))
            }

        } catch (e: Exception) {
            println("[FEEDBACK] Исключение: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}