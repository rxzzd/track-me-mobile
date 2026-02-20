package com.example.track_me_mobile.features.users.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.track_me_mobile.features.users.domain.models.TrackerUser
import com.example.track_me_mobile.features.users.domain.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackerListState(
    val users: List<TrackerUser> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showBlocked: Boolean = false
)

class TrackerListViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val allUsers = mutableListOf<TrackerUser>()

    private val _state = MutableStateFlow(TrackerListState())
    val state = _state.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            usersRepository.getTrackers(showBlocked = _state.value.showBlocked)
                .onSuccess { pagedResponse ->
                    println("TRACKER_LIST: Загружено ${pagedResponse.content.size} трекеров")

                    val users = pagedResponse.content.map { dto ->
                        TrackerUser(
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
                    println("TRACKER_LIST: Ошибка загрузки: ${error.message}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Не удалось загрузить трекеров"
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
            println("TRACKER_LIST: Подтверждение пользователя: $username")

            usersRepository.enableUser(username)
                .onSuccess {
                    println("TRACKER_LIST: Пользователь подтвержден, удаляем из списка")
                    // Удаляем из списка сразу после успешного enable
                    allUsers.removeAll { it.telegramNick == username }
                    onSearchQueryChanged(_state.value.searchQuery)
                }
                .onFailure { error ->
                    println("TRACKER_LIST: Ошибка подтверждения: ${error.message}")
                    _state.update { it.copy(error = "Не удалось подтвердить пользователя") }
                }
        }
    }

    fun deleteUser(username: String) {
        viewModelScope.launch {
            println("TRACKER_LIST: Удаление пользователя: $username")

            usersRepository.disableUser(username)
                .onSuccess {
                    println("TRACKER_LIST: Пользователь отключен, удаляем из списка")
                    // Удаляем из списка сразу после успешного disable
                    allUsers.removeAll { it.telegramNick == username }
                    onSearchQueryChanged(_state.value.searchQuery)
                }
                .onFailure { error ->
                    println("TRACKER_LIST: Ошибка удаления: ${error.message}")
                    _state.update { it.copy(error = "Не удалось удалить пользователя") }
                }
        }
    }
}