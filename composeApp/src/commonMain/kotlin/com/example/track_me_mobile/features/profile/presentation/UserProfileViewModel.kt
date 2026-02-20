package com.example.track_me_mobile.features.profile.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.track_me_mobile.features.users.domain.UsersRepository
import kotlinx.coroutines.launch

data class UserProfileState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val id: String? = null,
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val roles: List<String> = emptyList(),
    val enabled: Boolean = false
)

class UserProfileViewModel(
    private val usersRepository: UsersRepository,
    private val username: String
) : ViewModel() {

    var state by mutableStateOf(UserProfileState())
        private set

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            println("USER_PROFILE: Загрузка профиля пользователя: $username")

            usersRepository.getUserInfo(username)
                .onSuccess { userDto ->
                    println("USER_PROFILE: Профиль загружен: ${userDto.fullName}")
                    state = state.copy(
                        isLoading = false,
                        id = userDto.id ?: userDto.username, // fallback to username if id is null
                        username = userDto.username,
                        fullName = userDto.fullName,
                        email = userDto.email,
                        phoneNumber = userDto.phoneNumber ?: "Не указан",
                        roles = userDto.roles,
                        enabled = userDto.enabled
                    )
                }
                .onFailure { error ->
                    println("USER_PROFILE: Ошибка загрузки: ${error.message}")
                    state = state.copy(
                        isLoading = false,
                        error = "Не удалось загрузить профиль пользователя"
                    )
                }
        }
    }

    fun retry() {
        loadUserProfile()
    }
}