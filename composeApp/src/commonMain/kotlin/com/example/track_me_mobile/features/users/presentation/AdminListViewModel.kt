package com.example.track_me_mobile.features.users.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.track_me_mobile.features.users.domain.models.AdminUser
import com.example.track_me_mobile.features.users.domain.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminListState(
    val users: List<AdminUser> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showBlocked: Boolean = false
)

class AdminListViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val allUsers = mutableListOf<AdminUser>()

    private val _state = MutableStateFlow(AdminListState())
    val state = _state.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            usersRepository.getAdministrators(showBlocked = _state.value.showBlocked)
                .onSuccess { pagedResponse ->
                    println("ADMIN_LIST: Загружено ${pagedResponse.content.size} администраторов")

                    val users = pagedResponse.content.map { dto ->
                        AdminUser(
                            id          = dto.id ?: dto.username, // fallback to username if id is null
                            fullName    = dto.fullName,
                            telegramNick = dto.username,
                            isConfirmed = dto.enabled
                        )
                    }

                    allUsers.clear()
                    allUsers.addAll(users)

                    _state.update {
                        it.copy(
                            users = users,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    println("ADMIN_LIST: Ошибка загрузки: ${error.message}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Не удалось загрузить администраторов"
                        )
                    }
                }
        }
    }

    fun toggleBlockedFilter() {
        _state.update { it.copy(showBlocked = !it.showBlocked) }
        loadUsers()
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { currentState ->
            currentState.copy(
                searchQuery = query,
                users = if (query.isEmpty()) {
                    allUsers
                } else {
                    allUsers.filter { user ->
                        user.fullName.contains(query, ignoreCase = true) ||
                                user.telegramNick.contains(query, ignoreCase = true)
                    }
                }
            )
        }
    }

    fun confirmUser(username: String) {
        viewModelScope.launch {
            println("ADMIN_LIST: Подтверждение пользователя: $username")

            usersRepository.enableUser(username)
                .onSuccess {
                    println("ADMIN_LIST: Пользователь подтвержден, удаляем из списка")
                    // Удаляем из списка сразу после успешного enable
                    allUsers.removeAll { it.telegramNick == username }
                    onSearchQueryChanged(_state.value.searchQuery)
                }
                .onFailure { error ->
                    println("ADMIN_LIST: Ошибка подтверждения: ${error.message}")
                    _state.update { it.copy(error = "Не удалось подтвердить пользователя") }
                }
        }
    }

    fun deleteUser(username: String) {
        viewModelScope.launch {
            println("ADMIN_LIST: Удаление пользователя: $username")

            usersRepository.disableUser(username)
                .onSuccess {
                    println("ADMIN_LIST: Пользователь отключен, удаляем из списка")
                    // Удаляем из списка сразу после успешного disable
                    allUsers.removeAll { it.telegramNick == username }
                    onSearchQueryChanged(_state.value.searchQuery)
                }
                .onFailure { error ->
                    println("ADMIN_LIST: Ошибка удаления: ${error.message}")
                    _state.update { it.copy(error = "Не удалось удалить пользователя") }
                }
        }
    }
}