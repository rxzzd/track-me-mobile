package com.example.track_me_mobile.features.reports.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.reports.domain.StreamMeetingReportRepository
import com.example.track_me_mobile.features.reports.domain.models.StreamMeetingReportItem
import kotlinx.coroutines.launch

sealed class StreamMeetingReportState {
    object Loading : StreamMeetingReportState()
    data class Success(val items: List<StreamMeetingReportItem>) : StreamMeetingReportState()
    data class Error(val message: String) : StreamMeetingReportState()
}

class StreamMeetingReportViewModel(
    private val repository: StreamMeetingReportRepository,
    private val streamId: String
) : ScreenModel {

    var state by mutableStateOf<StreamMeetingReportState>(StreamMeetingReportState.Loading)
        private set

    var selectedTracker by mutableStateOf("Все")
        private set

    var selectedTeam by mutableStateOf("Все")
        private set

    var selectedStatus by mutableStateOf("Все")
        private set

    var availableTrackers by mutableStateOf<List<String>>(emptyList())
        private set

    var availableTeams by mutableStateOf<List<String>>(emptyList())
        private set

    var availableStatuses by mutableStateOf<List<String>>(emptyList())
        private set

    private val allItems = mutableListOf<StreamMeetingReportItem>()

    init {
        loadReports()
    }

    fun loadReports() {
        screenModelScope.launch {
            state = StreamMeetingReportState.Loading

            repository.getReportsByStream(
                streamId = streamId,
                page = 0,
                size = 10000,
                sort = listOf("teamName,asc", "startDate,desc")
            ).onSuccess { items ->
                allItems.clear()
                allItems.addAll(items)
                initializeFilters(items)
                applyFilters()
            }.onFailure { error ->
                println("[StreamMeetingReportVM] Ошибка загрузки отчётов: ${error.message}")
                state = StreamMeetingReportState.Error("Не удалось загрузить отчёт по встречам")
            }
        }
    }

    fun setTrackerFilter(selection: String) {
        selectedTracker = selection
        applyFilters()
    }

    fun setTeamFilter(selection: String) {
        selectedTeam = selection
        applyFilters()
    }

    fun setStatusFilter(selection: String) {
        selectedStatus = selection
        applyFilters()
    }

    private fun initializeFilters(items: List<StreamMeetingReportItem>) {
        availableTrackers = listOf("Все") + items
            .map { it.trackerFullName.orEmpty().ifBlank { it.trackerName.orEmpty() } }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith(String.CASE_INSENSITIVE_ORDER)

        availableTeams = listOf("Все") + items
            .map { it.teamName }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith(String.CASE_INSENSITIVE_ORDER)

        availableStatuses = listOf("Все") + items
            .map { it.teamStatus.orEmpty().ifBlank { it.status.orEmpty() } }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith(String.CASE_INSENSITIVE_ORDER)

        if (selectedTracker !in availableTrackers) selectedTracker = "Все"
        if (selectedTeam !in availableTeams) selectedTeam = "Все"
        if (selectedStatus !in availableStatuses) selectedStatus = "Все"
    }

    private fun applyFilters() {
        val filtered = allItems.filter { item ->
            val trackerName = item.trackerFullName.orEmpty().ifBlank { item.trackerName.orEmpty() }
            val status = item.teamStatus.orEmpty().ifBlank { item.status.orEmpty() }

            val trackerMatch = selectedTracker == "Все" || trackerName == selectedTracker
            val teamMatch = selectedTeam == "Все" || item.teamName == selectedTeam
            val statusMatch = selectedStatus == "Все" || status == selectedStatus

            trackerMatch && teamMatch && statusMatch
        }

        state = StreamMeetingReportState.Success(filtered)
    }

    suspend fun prepareExcelReport(): Result<Pair<String, ByteArray>> {
        val result = repository.downloadReportsExcel(
            streamId = streamId,
            trackerFilter = if (selectedTracker == "Все") null else selectedTracker,
            teamFilter = if (selectedTeam == "Все") null else selectedTeam,
            statusFilter = if (selectedStatus == "Все") null else selectedStatus,
            page = 0,
            size = 10000,
            sort = listOf("teamName,asc", "startDate,desc")
        )

        return result.mapCatching { bytes ->
            val fileName = "Отчёт_встречи_${streamId.take(8)}.xlsx"
            fileName to bytes
        }
    }
}
