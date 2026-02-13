package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel

class LoginViewModel : ScreenModel {

    // Состояние полей ввода
    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    // Функции для изменения текста (вызываются из UI)
    fun onUsernameChanged(newValue: String) {
        username = newValue
    }

    fun onPasswordChanged(newValue: String) {
        password = newValue
    }

    // Логика нажатия кнопки
    fun onLoginClick() {
        if (username.isNotEmpty() && password.isNotEmpty()) {
            println("Пытаемся войти с: $username")
            // Здесь в будущем будет вызов Repository
        }
    }
}