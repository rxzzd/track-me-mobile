package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.auth.domain.RegistrationRepository
import kotlinx.coroutines.launch

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    object Success : RegistrationState()           // показываем "Проверьте почту"
    data class Error(val message: String) : RegistrationState()
}

class RegistrationViewModel(
    private val repository: RegistrationRepository
) : ScreenModel {

    var state by mutableStateOf<RegistrationState>(RegistrationState.Idle)
        private set

    fun register(
        username: String,
        password: String,
        phoneNumber: String,
        fullName: String,
        email: String,
        role: String
    ) {
        screenModelScope.launch {
            state = RegistrationState.Loading

            repository.initRegistration(
                username    = username,
                password    = password,
                phoneNumber = phoneNumber,
                fullName    = fullName,
                email       = email,
                role        = role
            )
                .onSuccess {
                    state = RegistrationState.Success
                }
                .onFailure {
                    state = RegistrationState.Error("Не удалось зарегистрироваться. Проверьте данные.")
                }
        }
    }

    fun resetState() {
        state = RegistrationState.Idle
    }
}