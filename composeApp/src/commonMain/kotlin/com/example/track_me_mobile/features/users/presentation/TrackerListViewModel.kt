package com.example.track_me_mobile.features.tracker_list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.track_me_mobile.features.tracker_list.domain.models.TrackerUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackerListState(
    val users: List<TrackerUser> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class TrackerListViewModel : ViewModel() {
    // Тестовый список
    private val mockData = listOf(
        TrackerUser("1", "Иванов Иван Иванович", "ivan_tg", true),
        TrackerUser("2", "Петров Петр Петрович", "petr_tg", false),
        TrackerUser("3", "Сидоров Сидор", "sidor_tg", true),
        TrackerUser("4", "Смирнова Анна", "anna_news", true),
    )

    private val _state = MutableStateFlow(TrackerListState(users = mockData))
    val state = _state.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _state.update { currentState ->
            currentState.copy(
                searchQuery = query,
                users = if (query.isEmpty()) mockData else mockData.filter { user ->
                    user.fullName.contains(query, ignoreCase = true) ||
                            user.telegramNick.contains(query, ignoreCase = true)
                }
            )
        }
    }

    fun deleteUser(id: String) {
        _state.update { currentState ->
            currentState.copy(
                users = currentState.users.filter { user -> user.id != id }
            )
        }
    }

    fun loadUsers() {
        // Здесь будет загрузка с сервера
        viewModelScope.launch {
            // Имитация загрузки
            _state.update { it.copy(isLoading = true) }
            // ... загрузка данных
            _state.update { it.copy(isLoading = false, users = mockData) }
        }
    }
}