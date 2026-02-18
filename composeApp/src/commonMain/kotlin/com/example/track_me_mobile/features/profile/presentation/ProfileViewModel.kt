package com.example.track_me_mobile.features.profile.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.profile.domain.ProfileRepository
import com.example.track_me_mobile.features.profile.domain.models.UserProfile
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository
) : ScreenModel {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var profile by mutableStateOf<UserProfile?>(null)
        private set

    // Состояние сохранения (для кнопки "Сохранить" в ProfileEditScreen)
    var isSaving by mutableStateOf(false)
        private set

    var saveSuccess by mutableStateOf(false)
        private set

    init {
        loadProfile()
    }

    fun loadProfile() {
        screenModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getAccountInfo()
                .onSuccess { userProfile ->
                    profile = userProfile
                    isLoading = false
                }
                .onFailure {
                    errorMessage = "Не удалось загрузить профиль"
                    isLoading = false
                }
        }
    }

    fun saveProfile(
        fullName: String,
        email: String,
        phoneNumber: String,
        onSuccess: () -> Unit
    ) {
        screenModelScope.launch {
            isSaving = true
            errorMessage = null
            saveSuccess = false

            val currentAvatarUrl = profile?.avatarUrl

            println("PROFILE: Сохранение: name=$fullName, email=$email, phone=$phoneNumber")

            repository.updateAccount(fullName, email, phoneNumber, currentAvatarUrl)
                .onSuccess {
                    println("PROFILE: Успешно сохранено")
                    profile = profile?.copy(
                        fullName    = fullName,
                        email       = email,
                        phoneNumber = phoneNumber
                    )
                    isSaving = false
                    saveSuccess = true
                    onSuccess()
                }
                .onFailure { error ->
                    println("PROFILE: Ошибка сохранения: ${error.message}")
                    error.printStackTrace()
                    errorMessage = "Не удалось сохранить изменения"
                    isSaving = false
                }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}