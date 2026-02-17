package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.features.auth.data.AuthRepositoryImpl
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository,
    // AuthRepositoryImpl нужен напрямую для saveSession —
    // это data-операция, не входит в domain-интерфейс
    private val repositoryImpl: AuthRepositoryImpl
) : ScreenModel {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loginFromWebView(cookieString: String, onNavigate: (Role) -> Unit) {
        screenModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                repositoryImpl.saveSession(cookieString)

                repository.getUserInfo()
                    .onSuccess { userInfo ->
                        isLoading = false
                        onNavigate(userInfo.mainRole)
                    }
                    .onFailure { error ->
                        isLoading = false
                        errorMessage = "Не удалось загрузить профиль"
                    }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Произошла ошибка. Попробуйте ещё раз."
            }
        }
    }
}