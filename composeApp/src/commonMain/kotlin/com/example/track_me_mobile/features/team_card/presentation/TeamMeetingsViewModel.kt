package com.example.track_me_mobile.features.team_card.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.meetings.domain.MeetingRepository
import com.example.track_me_mobile.features.meetings.domain.models.Meeting
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

// Presentation модель для UI
data class MeetingItemUI(
    val id: String,
    val teamCardId: String,  // ← ДОБАВИТЬ
    val date: String,
    val title: String,
    val status: String,

    val number: String,
    val link: String,
    val teamStatus: String,
    val tasksCurrentMeeting: String,
    val tasksNextMeeting: String,
    val startDateIso: String
)

sealed class TeamMeetingsState {
    object Loading : TeamMeetingsState()
    data class Success(val meetings: List<MeetingItemUI>) : TeamMeetingsState()
    data class Error(val message: String) : TeamMeetingsState()
}

class TeamMeetingsViewModel(
    private val teamId: String,
    private val meetingRepository: MeetingRepository
) : ScreenModel {

    var state by mutableStateOf<TeamMeetingsState>(TeamMeetingsState.Loading)
        private set

    var isCreating by mutableStateOf(false)
        private set

    init {
        loadMeetings()
    }

    fun loadMeetings() {
        screenModelScope.launch {
            state = TeamMeetingsState.Loading

            meetingRepository.getMeetings(teamCardId = teamId, page = 0, size = 100)
                .onSuccess { page ->
                    // Маппим domain -> presentation
                    val uiMeetings = page.content
                        .sortedByDescending { it.number.toIntOrNull() ?: 0 }
                        .map { it.toUI() }

                    state = TeamMeetingsState.Success(uiMeetings)
                }
                .onFailure { e ->
                    state = TeamMeetingsState.Error(e.message ?: "Ошибка загрузки")
                }
        }
    }

    fun planNewMeeting(startDateIso: String, onSuccess: () -> Unit) {
        val state = state as? TeamMeetingsState.Success ?: return

        // ИСПРАВЛЕНИЕ: находим максимальный номер среди существующих встреч
        val maxNumber = state.meetings
            .mapNotNull { it.number.toIntOrNull() }
            .maxOrNull() ?: 0
        val nextNumber = (maxNumber + 1).toString()

        println("[TeamMeetingsVM] Creating meeting with number: $nextNumber (max existing: $maxNumber)")

        screenModelScope.launch {
            isCreating = true
            meetingRepository.createMeeting(teamId, startDateIso, nextNumber)
                .onSuccess {
                    println("[TeamMeetingsVM] Meeting created successfully with number: $nextNumber")
                    onSuccess()
                    loadMeetings()
                }
                .onFailure { e ->
                    println("[TeamMeetingsVM] Failed to create meeting: ${e.message}")
                }
            isCreating = false
        }
    }

    fun deleteMeeting(meetingId: String, onSuccess: () -> Unit) {
        screenModelScope.launch {
            println("[TeamMeetingsVM] Deleting meeting: $meetingId")

            meetingRepository.deleteMeeting(meetingId)
                .onSuccess {
                    println("[TeamMeetingsVM] Meeting deleted successfully")
                    onSuccess()
                    loadMeetings() // Перезагружаем список
                }
                .onFailure { e ->
                    println("[TeamMeetingsVM] Delete failed: ${e.message}")
                    // Можно добавить state для ошибок
                }
        }
    }

    // Маппинг domain -> presentation
    private fun Meeting.toUI(): MeetingItemUI {
        val formattedDate = try {
            val instant = Instant.parse(startDate)
            val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            "${localDate.dayOfMonth.toString().padStart(2, '0')}.${localDate.monthNumber.toString().padStart(2, '0')}.${localDate.year}"
        } catch (e: Exception) {
            startDate.take(10)
        }

        return MeetingItemUI(
            id = id,
            teamCardId = teamCardId,
            date = formattedDate,
            title = "Встреча №$number",
            status = status,

            number = number,
            link = link,
            teamStatus = teamStatus,
            tasksCurrentMeeting = tasksCurrentMeeting,
            tasksNextMeeting = tasksNextMeeting,
            startDateIso = startDate
        )
    }

    fun updateMeetingDate(meeting: MeetingItemUI, newDateMillis: Long, onSuccess: () -> Unit) {
        screenModelScope.launch {
            println("[TeamMeetingsVM] Updating date for meeting: ${meeting.id}")

            try {
                // Конвертируем миллисекунды в ISO дату
                val newInstant = kotlinx.datetime.Instant.fromEpochMilliseconds(newDateMillis)
                val newLocalDate = newInstant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date

                // Берем время из старой даты
                val oldInstant = kotlinx.datetime.Instant.parse(meeting.startDateIso)
                val oldTime = oldInstant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).time

                // Комбинируем новую дату со старым временем
                val newDateTime = kotlinx.datetime.LocalDateTime(newLocalDate, oldTime)
                val newIsoDate = newDateTime.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toString()

                println("[TeamMeetingsVM] Old date: ${meeting.startDateIso}")
                println("[TeamMeetingsVM] New date: $newIsoDate")

                // ДОБАВИТЬ ЛОГИРОВАНИЕ ВСЕХ ПОЛЕЙ
                println("[TeamMeetingsVM] ========== UPDATE DATE REQUEST ==========")
                println("[TeamMeetingsVM] link: '${meeting.link}'")
                println("[TeamMeetingsVM] number: '${meeting.number}'")
                println("[TeamMeetingsVM] teamStatus: '${meeting.teamStatus}'")
                println("[TeamMeetingsVM] tasksCurrentMeeting: '${meeting.tasksCurrentMeeting}'")
                println("[TeamMeetingsVM] tasksNextMeeting: '${meeting.tasksNextMeeting}'")
                println("[TeamMeetingsVM] startDate: '$newIsoDate'")
                println("[TeamMeetingsVM] status: '${meeting.status}'")
                println("[TeamMeetingsVM] ===========================================")

                // Проверяем что все обязательные поля заполнены
                val safeLink = if (meeting.link.isBlank()) "https://example.com" else meeting.link
                val safeTasks = if (meeting.tasksCurrentMeeting.isBlank()) "Не указано" else meeting.tasksCurrentMeeting
                val safeTasksNext = if (meeting.tasksNextMeeting.isBlank()) "Не указано" else meeting.tasksNextMeeting

                println("[TeamMeetingsVM] Using safe values:")
                println("[TeamMeetingsVM] safeLink: '$safeLink'")
                println("[TeamMeetingsVM] safeTasks: '$safeTasks'")
                println("[TeamMeetingsVM] safeTasksNext: '$safeTasksNext'")

                // Создаем request с обновленной датой
                val request = com.example.track_me_mobile.features.meetings.domain.models.MeetingUpdateRequest(
                    id = meeting.id,
                    link = safeLink,
                    number = meeting.number,
                    teamStatus = meeting.teamStatus,
                    tasksCurrentMeeting = safeTasks,
                    tasksNextMeeting = safeTasksNext,
                    startDate = newIsoDate,
                    status = meeting.status,
                    teamCardId = meeting.teamCardId
                )

                meetingRepository.updateMeeting(meeting.id, meeting.teamCardId, request)
                    .onSuccess {
                        println("[TeamMeetingsVM] Date updated successfully")
                        onSuccess()
                        loadMeetings()
                    }
                    .onFailure { e ->
                        println("[TeamMeetingsVM] Update date failed: ${e.message}")
                        e.printStackTrace()
                    }
            } catch (e: Exception) {
                println("[TeamMeetingsVM] Date conversion error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}