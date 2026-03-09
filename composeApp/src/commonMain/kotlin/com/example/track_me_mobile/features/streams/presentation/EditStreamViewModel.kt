package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.streams.data.StreamHasTeamsException
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
import kotlinx.coroutines.launch

class EditStreamViewModel(
    private val repository: StreamRepository,
    private val streamId: String
) : ScreenModel, StreamMarketState {

    var name by mutableStateOf("")
    var startDate by mutableStateOf("")
    var endDate by mutableStateOf("")
    var trackStartDate by mutableStateOf("")
    var meetingsCount by mutableStateOf(0)

    override var availableMarkets by mutableStateOf<List<NtiMarket>>(emptyList())
    override var selectedMarketIds by mutableStateOf<Set<String>>(emptySet())

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // НОВОЕ: Состояние для диалога с командами
    var showTeamsConflictDialog by mutableStateOf(false)
        private set

    var teamsInStream by mutableStateOf<List<TeamCard>>(emptyList())
        private set

    init {
        loadMarkets()
        loadStream()
    }

    private fun loadMarkets() {
        screenModelScope.launch {
            repository.getNtiMarkets()
                .onSuccess { markets ->
                    availableMarkets = markets
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    private fun loadStream() {
        screenModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getStream(streamId)
                .onSuccess { stream ->
                    name = stream.name
                    startDate = stream.startDate
                    endDate = stream.endDate
                    trackStartDate = stream.trackStartDate
                    meetingsCount = stream.meetingsCount
                    selectedMarketIds = stream.ntiMarkets.map { it.id }.toSet()
                }
                .onFailure {
                    errorMessage = it.message ?: "Не удалось загрузить поток"
                }

            isLoading = false
        }
    }

    override fun toggleMarket(id: String) {
        selectedMarketIds = if (id in selectedMarketIds) {
            selectedMarketIds - id
        } else {
            selectedMarketIds + id
        }
    }

    fun updateStream(onSuccess: () -> Unit) {
        if (name.isBlank() || startDate.isBlank() || endDate.isBlank() || trackStartDate.isBlank()) {
            errorMessage = "Заполните обязательные поля"
            return
        }

        screenModelScope.launch {
            isLoading = true
            errorMessage = null

            val request = StreamCreateRequest(
                name = name,
                startDate = startDate,
                endDate = endDate,
                ntiMarketIds = selectedMarketIds.toList(),
                description = "",
                trackStartDate = trackStartDate,
                meetingsCount = meetingsCount
            )

            repository.updateStream(streamId, request)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { exception ->
                    val rawError = exception.message ?: ""

                    errorMessage = if (rawError.contains("trackStartDate: Дата начала трека должна быть в будущем")) {
                        "Дата начала трека должна быть в будущем"
                    } else {
                        "Ошибка при сохранении: проверьте корректность данных"
                    }
                }

            isLoading = false
        }
    }

    fun deleteStream(onSuccess: () -> Unit) {
        screenModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.deleteStream(streamId)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { exception ->
                    if (exception is StreamHasTeamsException) {
                        // Поток содержит команды - загружаем их и показываем диалог
                        loadTeamsAndShowDialog()
                    } else {
                        errorMessage = exception.message ?: "Не удалось удалить поток"
                    }
                }

            isLoading = false
        }
    }

    private fun loadTeamsAndShowDialog() {
        screenModelScope.launch {
            repository.getTeamsByStream(streamId)
                .onSuccess { teams ->
                    if (teams.isEmpty()) {
                        // Команды есть в БД, но не загрузились через API - показываем общее сообщение
                        errorMessage = "В потоке есть команды. Удалите команды перед удалением потока."
                    } else {
                        // Команды загружены - показываем диалог со списком
                        teamsInStream = teams
                        showTeamsConflictDialog = true
                    }
                }
                .onFailure {
                    // Ошибка загрузки - показываем общее сообщение
                    errorMessage = "В потоке есть команды. Удалите команды перед удалением потока."
                }
        }
    }

    fun dismissTeamsDialog() {
        showTeamsConflictDialog = false
    }
}