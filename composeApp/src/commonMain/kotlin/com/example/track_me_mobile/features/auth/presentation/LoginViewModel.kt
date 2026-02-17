package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ScreenModel {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun startAuth() {
        isLoading = true
        errorMessage = null
    }

    fun setError(message: String) {
        isLoading = false
        errorMessage = message
    }


    fun loginFromWebView(cookieString: String, onNavigate: (Role) -> Unit) {
        screenModelScope.launch {
            // Используем println для логирования в общем коде
            println("VM_AUTH: Начало loginFromWebView. Кука получена.")
            isLoading = true
            errorMessage = null

            try {
                repository.syncSession(cookieString)
                println("VM_AUTH: Сессия синхронизирована в Ktor.")

                val result = repository.getUserInfo()

                result.onSuccess { userInfo ->
                    println("VM_AUTH: УСПЕХ! Профиль получен. Роль: ${userInfo.mainRole}")
                    isLoading = false
                    // Это тот самый вызов, который закрывает WebView и переходит дальше
                    onNavigate(userInfo.mainRole)
                }

                result.onFailure { error ->
                    println("VM_AUTH: ПРОВАЛ! Ошибка API: ${error.message}")
                    isLoading = false
                    errorMessage = "Ошибка профиля: ${error.message}"
                }
            } catch (e: Exception) {
                println("VM_AUTH: КРИТИЧЕСКАЯ ОШИБКА: ${e.message}")
                isLoading = false
                errorMessage = e.message
            }
        }
    }
}