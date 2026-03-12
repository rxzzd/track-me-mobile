package com.example.track_me_mobile.features.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.network.SessionStorage
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    private val sessionStorage: SessionStorage,
    private val authRepository: AuthRepository,
    private val userInfoHolder: UserInfoHolder
) : ScreenModel {

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun checkAuth(onResult: (SplashDestination) -> Unit) {
        screenModelScope.launch {
            try {
                println("[Splash] Checking session...")

                // Проверяем наличие SESSION cookie
                val sessionCookie = sessionStorage.get()

                if (sessionCookie.isNullOrBlank()) {
                    println("[Splash] No session found → Login")
                    delay(500) // Минимальная задержка для плавности
                    onResult(SplashDestination.Login)
                    return@launch
                }

                println("[Splash] Session exists, validating...")

                // Проверяем валидность сессии через getUserInfo
                authRepository.getUserInfo()
                    .onSuccess { userInfo ->
                        println("[Splash] Session valid! User: ${userInfo.username}, Role: ${userInfo.mainRole}")

                        // Сохраняем UserInfo
                        userInfoHolder.save(userInfo)

                        // Определяем куда перенаправить
                        val destination = when (userInfo.mainRole) {
                            Role.ADMIN, Role.SUPER_ADMIN -> SplashDestination.AdminHome
                            Role.TRACKER -> SplashDestination.TrackerHome
                            else -> SplashDestination.Login
                        }

                        delay(500)
                        onResult(destination)
                    }
                    .onFailure { e ->
                        println("[Splash] Session invalid: ${e.message}")
                        errorMessage = "Сессия истекла"

                        // Очищаем невалидную сессию
                        sessionStorage.clear()

                        delay(1000)
                        onResult(SplashDestination.Login)
                    }

            } catch (e: Exception) {
                println("[Splash] Error: ${e.message}")
                e.printStackTrace()
                errorMessage = "Ошибка проверки авторизации"

                delay(1000)
                onResult(SplashDestination.Login)
            }
        }
    }
}