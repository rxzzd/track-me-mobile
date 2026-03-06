package com.example.track_me_mobile.core.feedback.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.feedback.domain.FeedbackRepository
import com.example.track_me_mobile.core.feedback.domain.models.FeedbackData
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val repository: FeedbackRepository,
    private val userInfoHolder: UserInfoHolder
) : ScreenModel {

    var designRating by mutableStateOf("")
        private set

    var usabilityRating by mutableStateOf("")
        private set

    var bugsCount by mutableStateOf("0")
        private set

    var liked by mutableStateOf("")
        private set

    var disliked by mutableStateOf("")
        private set

    var suggestions by mutableStateOf("")
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var isSubmitted by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun updateDesignRating(rating: String) {
        designRating = rating
    }

    fun updateUsabilityRating(rating: String) {
        usabilityRating = rating
    }

    fun updateBugsCount(count: String) {
        if (count.all { it.isDigit() }) {
            bugsCount = count
        }
    }

    fun updateLiked(text: String) {
        liked = text
    }

    fun updateDisliked(text: String) {
        disliked = text
    }
    fun resetSubmitted() {
        isSubmitted = false
    }
    fun updateSuggestions(text: String) {
        suggestions = text
    }

    fun submitFeedback(onSuccess: () -> Unit) {
        screenModelScope.launch {
            println("[FEEDBACK_VM] Попытка отправки. Design: '$designRating', Usability: '$usabilityRating'")

            // Валидация
            if (designRating.isEmpty() || usabilityRating.isEmpty()) {
                println("[FEEDBACK_VM] Ошибка валидации: поля пусты")
                errorMessage = "Пожалуйста, заполните обязательные поля"
                return@launch
            }

            isSubmitting = true
            errorMessage = null

            val userInfo = userInfoHolder.userInfo

            val feedback = FeedbackData(
                designRating = designRating.toIntOrNull() ?: 0,
                usabilityRating = usabilityRating.toIntOrNull() ?: 0,
                bugsCount = bugsCount.toIntOrNull() ?: 0,
                liked = liked,
                disliked = disliked,
                suggestions = suggestions,
                username = userInfo?.username ?: "",
                email = userInfo?.email ?: "",
                fullName = userInfo?.fullName ?: ""
            )

            repository.sendFeedback(feedback)
                .onSuccess {
                    isSubmitted = true
                    isSubmitting = false
                    resetForm()
                    onSuccess()
                }
                .onFailure { error ->
                    errorMessage = "Ошибка отправки: ${error.message}"
                    isSubmitting = false
                }
        }
    }

    private fun resetForm() {
        designRating = ""
        usabilityRating = ""
        bugsCount = "0"
        liked = ""
        disliked = ""
        suggestions = ""
    }


}