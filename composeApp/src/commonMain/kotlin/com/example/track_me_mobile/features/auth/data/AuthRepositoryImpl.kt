package com.example.track_me_mobile.features.auth.data

import com.example.track_me_mobile.features.auth.domain.AuthRepository
import kotlinx.coroutines.delay

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(username: String, password: String): Result<Boolean> {
        delay(2000)
        return if (username == "admin" && password == "123") {
            Result.success(true)
        } else {
            Result.failure(Exception("Неверный логин или пароль"))
        }
    }
}