package com.example.track_me_mobile.features.reports.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.features.reports.domain.ReportRepository
import com.example.track_me_mobile.features.reports.domain.models.ReportItem
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.core.network.ApiConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

sealed class ReportsState {
    object Loading : ReportsState()
    data class Success(val reports: List<ReportItem>) : ReportsState()
    data class Error(val message: String) : ReportsState()
}

@Serializable
data class TrackerDto(
    val id: String,
    val username: String,
    val fullName: String? = null
)

@Serializable
data class TrackersPageDto(
    val content: List<TrackerDto>
)

class ReportsViewModel(
    private val repository: ReportRepository,
    private val userInfoHolder: UserInfoHolder,
    private val httpClient: HttpClient
) : ScreenModel {

    var state by mutableStateOf<ReportsState>(ReportsState.Loading)
        private set

    var selectedTracker by mutableStateOf("Все")
        private set

    var selectedStream by mutableStateOf("Все")
        private set

    var showInactive by mutableStateOf(false)
        private set

    var availableTrackers by mutableStateOf<List<TrackerInfo>>(emptyList())
        private set

    var availableStreams by mutableStateOf<List<String>>(emptyList())
        private set

    data class TrackerInfo(
        val username: String,
        val fullName: String
    )

    init {
        loadTrackers()
        loadReports()
    }

    private fun loadTrackers() {
        screenModelScope.launch {
            try {
                println("REPORTS_VM: Загрузка трекеров...")

                // Получаем CSRF токен
                val csrfResponse = httpClient.get(ApiConstants.CSRF_ENDPOINT) {
                    header(HttpHeaders.Accept, "application/json")
                }
                val csrfData = csrfResponse.body<CsrfResponse>()

                // ИСПРАВЛЕНО: используем правильный эндпоинт POST /users/trackers с пагинацией
                val response = httpClient.post("https://api.trackme.test.startup-poligon.com/sso/api/v1/users/trackers?page=0&size=1000&sort=username,asc") {
                    header(HttpHeaders.Accept, "application/json")
                    header(csrfData.headerName, csrfData.token)
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("filters" to emptyList<String>()))
                }

                if (response.status == HttpStatusCode.OK) {
                    val trackersPage = response.body<TrackersPageDto>()

                    val trackers = trackersPage.content.map {
                        TrackerInfo(it.username, it.fullName ?: it.username)
                    }

                    availableTrackers = trackers
                    println("REPORTS_VM: Загружено ${trackers.size} трекеров")
                } else {
                    println("REPORTS_VM: Ошибка загрузки трекеров: ${response.status}")
                }
            } catch (e: Exception) {
                println("REPORTS_VM: Ошибка загрузки трекеров: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun loadReports() {
        screenModelScope.launch {
            state = ReportsState.Loading

            val trackerFilter = if (selectedTracker == "Все") null else selectedTracker
            val streamFilter = if (selectedStream == "Все") null else selectedStream

            repository.getReports(
                trackerUsername = trackerFilter,
                streamName = streamFilter,
                showInactive = showInactive
            )
                .onSuccess { reports ->
                    updateFilterOptions(reports)
                    state = ReportsState.Success(reports)
                }
                .onFailure { error ->
                    println("REPORTS_VM: Ошибка: ${error.message}")
                    state = ReportsState.Error("Не удалось загрузить отчёты")
                }
        }
    }

    fun setTrackerFilter(tracker: String) {
        selectedTracker = tracker
        loadReports()
    }

    fun setStreamFilter(stream: String) {
        selectedStream = stream
        loadReports()
    }

    fun toggleShowInactive() {
        showInactive = !showInactive
        loadReports()
    }

    private fun updateFilterOptions(reports: List<ReportItem>) {
        val streams = reports.map { it.streamName }.distinct().sorted()
        availableStreams = listOf("Все") + streams
    }

    fun downloadReport() {
        println("REPORTS_VM: Выгрузка отчёта...")
    }
}