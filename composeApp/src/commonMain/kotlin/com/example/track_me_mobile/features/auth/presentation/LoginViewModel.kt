package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.network.logDebug
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ScreenModel {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Функция для проверки роли ПОСЛЕ того, как пользователь вошел через WebView
    fun checkAuthAndNavigate(onNavigate: (Role) -> Unit) {
        screenModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getUserInfo()
                .onSuccess { userInfo ->
                    isLoading = false
                    val role = userInfo.mainRole
                    if (role == Role.UNKNOWN) {
                        errorMessage = "Доступ запрещен: роль не определена"
                    } else {
                        onNavigate(role)
                    }
                }
                .onFailure { throwable ->
                    // ВОТ ТУТ МЫ ДОЛЖНЫ ВЫЗВАТЬ ОБРАБОТЧИК
                    handleAuthError(throwable)
                }
        }
    }

    fun loginFromWebView(cookieString: String, onNavigate: (Role) -> Unit) {
        screenModelScope.launch {
            try {
                isLoading = true
                repository.syncSession(cookieString)
                checkAuthAndNavigate(onNavigate)
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Ошибка при входе: ${e.message}"
                logDebug("DEBUG_TAG: Краш во ViewModel подавлен: ${e.message}")
            }
        }
    }


    private fun handleAuthError(throwable: Throwable) {
        isLoading = false
        // Выведи текст ошибки из исключения:
        errorMessage = throwable.message ?: "Неизвестная ошибка"
        println("DEBUG_TAG: Детальная ошибка -> ${throwable.stackTraceToString()}")
    }
}