package com.example.track_me_mobile.features.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.network.SessionProvider
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    private val sessionStorage: SessionProvider,
    private val authRepository: AuthRepository,
    private val userInfoHolder: UserInfoHolder
) : ScreenModel {

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun checkAuth(onResult: (SplashDestination) -> Unit) {
        screenModelScope.launch {
            try {
                println("[Splash] Checking session...")

                val sessionCookie = sessionStorage.get()

                if (sessionCookie.isNullOrBlank()) {
                    println("[Splash] No session found → Login")
                    delay(500)
                    onResult(SplashDestination.Login)
                    return@launch
                }

                println("[Splash] Session exists, validating...")

                authRepository.getUserInfo()
                    .onSuccess { userInfo ->
                        println("[Splash] Session valid! User: ${userInfo.username}, Role: ${userInfo.mainRole}")

                        userInfoHolder.save(userInfo)

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