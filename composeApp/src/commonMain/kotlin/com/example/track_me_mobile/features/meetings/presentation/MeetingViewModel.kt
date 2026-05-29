package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.meetings.domain.MeetingRepository
import com.example.track_me_mobile.features.meetings.domain.models.Meeting
import com.example.track_me_mobile.features.meetings.domain.models.MeetingUpdateRequest
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
import kotlinx.coroutines.launch

class MeetingViewModel(
    private val repository: MeetingRepository,
    private val meetingId: String,
    private val teamCardId: String,
    private val teamCardRepository: TeamCardRepository,
    ) : ScreenModel {

    var meeting by mutableStateOf<Meeting?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        println("[MeetingVM] init with meetingId=$meetingId, teamCardId=$teamCardId")
        loadMeetings()
        loadTeamCard()
    }

    var meetingRoomLink by mutableStateOf<String?>(null)
        private set

    fun loadTeamCard(){
        screenModelScope.launch {
            teamCardRepository.getTeamById(teamCardId)
                .onSuccess { teamCard ->
                    meetingRoomLink = teamCard.meetingRoomLink
                }
                .onFailure { e ->
                    println("[MeetingVM] Ошибка загрузки карточки команды: ${e.message}")
                }
        }
    }


    fun loadMeetings() {
        screenModelScope.launch {
            isLoading = true
            errorMessage = null

            println("[MeetingVM] Loading meetings for teamCardId=$teamCardId")

            // ✅ Передаем правильный teamCardId
            repository.getMeetings(teamCardId = teamCardId, page = 0, size = 100)
                .onSuccess { page ->
                    println("[MeetingVM] Loaded ${page.content.size} meetings for team")

                    meeting = page.content.find { it.id == meetingId }

                    if (meeting != null) {
                        println("[MeetingVM] Found meeting: ${meeting!!.number}, status=${meeting!!.status}")
                    } else {
                        println("[MeetingVM] ERROR: Meeting not found! Available IDs: ${page.content.map { it.id }}")
                        errorMessage = "Встреча не найдена (ID: $meetingId)"
                    }
                }
                .onFailure { e ->
                    println("[MeetingVM] ERROR loading meetings: ${e.message}")
                    e.printStackTrace()
                    errorMessage = e.message ?: "Ошибка загрузки"
                }

            isLoading = false
            println("[MeetingVM] Loading complete. Meeting found: ${meeting != null}")
        }
    }

    // Остальные методы без изменений (updateMeeting, updateIsoDateWithUi)
    fun updateMeeting(
        meetingId: String,
        tasksNext: String,
        tasksCurrent: String,
        teamStatusUI: String,
        link: String,
        uiDate: String,
        meetingStatusUI: String
    ) {
        val currentMeeting = meeting ?: run {
            println("[MeetingVM] Cannot update - meeting is null")
            return
        }

        screenModelScope.launch {
            isLoading = true
            println("[MeetingVM] Updating meeting $meetingId")

            val apiTeamStatus = when (teamStatusUI) {
                "Есть проблемы" -> "WITH_ISSUES"
                "Есть большие проблемы" -> "MANY_ISSUES"
                "Всё ок" -> "OK"
                else -> "OK"
            }

            val apiStatus = when (meetingStatusUI) {
                "Состоялась" -> "COMPLETED"
                "Не состоялась" -> "COMPLETED_AS_NOT_HAPPENED"
                else -> "SCHEDULED"
            }

            println("[MeetingVM] Mapped status: UI '$meetingStatusUI' -> API '$apiStatus'")
            println("[MeetingVM] Mapped team status: UI '$teamStatusUI' -> API '$apiTeamStatus'")

            val updatedIsoDate = updateIsoDateWithUi(currentMeeting.startDate, uiDate)

            val request = MeetingUpdateRequest(
                id = currentMeeting.id,
                link = link,
                number = currentMeeting.number,
                teamStatus = apiTeamStatus,
                tasksCurrentMeeting = tasksCurrent,
                tasksNextMeeting = tasksNext,
                startDate = updatedIsoDate,
                status = apiStatus,
                teamCardId = currentMeeting.teamCardId
            )

            println("[MeetingVM] ========== REQUEST BODY ==========")
            println("[MeetingVM] link: '$link'")
            println("[MeetingVM] number: '${currentMeeting.number}'")
            println("[MeetingVM] teamStatus: '$apiTeamStatus'")
            println("[MeetingVM] tasksCurrentMeeting: '$tasksCurrent'")
            println("[MeetingVM] tasksNextMeeting: '$tasksNext'")
            println("[MeetingVM] startDate: '$updatedIsoDate'")
            println("[MeetingVM] status: '$apiStatus'")
            println("[MeetingVM] ===================================")

            repository.updateMeeting(meetingId, currentMeeting.teamCardId, request)
                .onSuccess {
                    println("[MeetingVM] Update successful, reloading...")
                    loadMeetings()
                }
                .onFailure { e ->
                    println("[MeetingVM] Update failed: ${e.message}")
                    errorMessage = e.message ?: "Ошибка сохранения"
                }

            isLoading = false
        }
    }

    private fun updateIsoDateWithUi(oldIso: String, uiDate: String): String {
        try {
            val parts = uiDate.split(".")
            if (parts.size >= 2) {
                val day = parts[0].padStart(2, '0')
                val month = parts[1].padStart(2, '0')
                val updated = oldIso.replaceRange(5, 10, "$month-$day")
                println("[MeetingVM] Date update: '$uiDate' -> '$updated'")
                return updated
            }
        } catch (e: Exception) {
            println("[MeetingVM] Date parsing error: ${e.message}")
        }
        return oldIso
    }

    fun uploadImage(imageBytes: ByteArray) {
        val currentMeeting = meeting ?: run {
            println("[MeetingVM] Cannot upload image - meeting is null")
            return
        }

        screenModelScope.launch {
            isLoading = true
            println("[MeetingVM] Uploading image for meeting: $meetingId, size: ${imageBytes.size} bytes")

            repository.uploadImage(meetingId, imageBytes)
                .onSuccess {
                    println("[MeetingVM] Image uploaded successfully")
                    println("[MeetingVM] OLD imageUrl: ${meeting?.imageUrl}")  // ← ДОБАВИТЬ

                    // Перезагружаем встречу чтобы получить новый imageUrl
                    loadMeetings()

                    // ДОБАВИТЬ: Логируем после перезагрузки
                    kotlinx.coroutines.delay(500) // Ждем загрузки
                    println("[MeetingVM] NEW imageUrl: ${meeting?.imageUrl}")  // ← ДОБАВИТЬ
                }
                .onFailure { e ->
                    println("[MeetingVM] Image upload failed: ${e.message}")
                    errorMessage = "Ошибка загрузки изображения: ${e.message}"
                }

            isLoading = false
        }
    }
}