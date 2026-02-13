package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.domain.AuthError
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ScreenModel {

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onUsernameChanged(newValue: String) {
        username = newValue
        errorMessage = null
    }

    fun onPasswordChanged(newValue: String) {
        password = newValue
        errorMessage = null
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Заполните все поля"
            return
        }

        screenModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getCsrfToken()
                .onSuccess { csrf ->
                    repository.login(username, password, csrf)
                        .onSuccess {
                            isLoading = false
                            onSuccess()
                        }
                        .onFailure { handleAuthError(it) }
                }
                .onFailure { handleAuthError(it) }
        }
    }

    private fun handleAuthError(throwable: Throwable) {
        isLoading = false
        errorMessage = when (throwable) {
            is AuthError.InvalidCredentials -> "Неверный логин или пароль"
            is AuthError.NetworkError -> "Нет соединения с сервером"
            else -> "Произошла непредвиденная ошибка"
        }
    }
}