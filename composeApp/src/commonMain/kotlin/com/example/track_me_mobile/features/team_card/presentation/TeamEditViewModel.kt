package com.example.track_me_mobile.features.team_card.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
import com.example.track_me_mobile.features.team_card.domain.models.TrackerUser
import com.example.track_me_mobile.features.team_card.domain.models.UpdateTeamRequest
import com.example.track_me_mobile.features.teams.domain.models.NtiMarket
import com.example.track_me_mobile.features.teams.domain.models.Stream
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TeamEditUiState(
    val isLoading: Boolean = true,
    val loadError: String? = null,

    val originalTeam: TeamCard? = null,

    val availableTrackers: List<TrackerUser> = emptyList(),
    val availableStreams: List<Stream> = emptyList(),
    val availableMarkets: List<NtiMarket> = emptyList(),

    val name: String = "",
    val meetingRoomLink: String = "",
    val description: String = "",
    val selectedTracker: TrackerUser? = null,
    val selectedStream: Stream? = null,
    val selectedMarkets: List<NtiMarket> = emptyList(),
    val selectedTrl: String = "",

    val isAdminRole: Boolean = false,

    val nameError: String? = null,
    val meetingRoomLinkError: String? = null,
    val trackerError: String? = null,
    val streamError: String? = null,
    val marketsError: String? = null,
    val trlError: String? = null,
    val descriptionError: String? = null,

    val isSubmitting: Boolean = false,
    val submitError: String? = null,

    val isDeleting: Boolean = false,
    val deleteError: String? = null,
)

class TeamEditViewModel(
    private val teamId: String,
    private val repository: TeamCardRepository,
    private val userInfoHolder: UserInfoHolder
) : ScreenModel {

    private val _state = MutableStateFlow(TeamEditUiState())
    val state: StateFlow<TeamEditUiState> = _state.asStateFlow()

    private val isAdmin get() = userInfoHolder.userInfo?.mainRole.let {
        it == Role.ADMIN || it == Role.SUPER_ADMIN
    }

    init { loadData() }

    fun retry() = loadData()

    private fun loadData() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, loadError = null) }

            val teamResult = repository.getTeamById(teamId)
            if (teamResult.isFailure) {
                _state.update { it.copy(isLoading = false, loadError = "Не удалось загрузить карточку команды") }
                return@launch
            }


            val marketsResult = repository.getNtiMarkets()
            if (marketsResult.isFailure) {
                _state.update { it.copy(isLoading = false, loadError = "Не удалось загрузить рынки НТИ") }
                return@launch
            }

            val team = teamResult.getOrThrow()
            val allMarkets = marketsResult.getOrElse { emptyList() }
            val currentMarkets = allMarkets.filter { m -> team.ntiMarkets.any { it.id == m.id } }

            var trackers: List<TrackerUser> = emptyList()
            var streams: List<Stream> = emptyList()
            var selectedTracker: TrackerUser? = null
            var selectedStream: Stream? = null

            if (isAdmin) {
                val trackersResult = repository.getTrackers()
                if (trackersResult.isFailure) {
                    _state.update { it.copy(isLoading = false, loadError = "Не удалось загрузить трекеров") }
                    return@launch
                }
                val streamsResult = repository.getStreams()
                if (streamsResult.isFailure) {
                    _state.update { it.copy(isLoading = false, loadError = "Не удалось загрузить потоки") }
                    return@launch
                }
                trackers = trackersResult.getOrElse { emptyList() }
                streams  = streamsResult.getOrElse { emptyList() }
                selectedTracker = trackers.find { it.username == team.username }
                    ?: TrackerUser(id = team.username, username = team.username, fullName = team.username, email = "", avatarUrl = null)
                selectedStream  = streams.find { it.id == team.stream?.id } ?: team.stream
            }

            _state.update {
                it.copy(
                    isLoading          = false,
                    originalTeam       = team,
                    availableTrackers  = trackers,
                    availableStreams    = streams,
                    availableMarkets   = allMarkets,
                    name               = team.name,
                    meetingRoomLink = team.meetingRoomLink,
                    description        = team.description,
                    selectedTracker    = selectedTracker,
                    selectedStream     = selectedStream,
                    selectedMarkets    = currentMarkets,
                    selectedTrl        = team.readinessLevel,
                    isAdminRole        = isAdmin
                )
            }
        }
    }

    fun onNameChange(v: String)               = _state.update { it.copy(name = v, nameError = null) }
    fun onMeetingRoomLinkChange(v: String) = _state.update { it.copy(meetingRoomLink = v, meetingRoomLinkError = null) }
    fun onDescriptionChange(v: String)        = _state.update { it.copy(description = v, descriptionError = null) }
    fun onTrackerSelected(v: TrackerUser)     = _state.update { it.copy(selectedTracker = v, trackerError = null) }
    fun onStreamSelected(v: Stream)           = _state.update { it.copy(selectedStream = v, streamError = null) }
    fun onMarketsChanged(v: List<NtiMarket>)  = _state.update { it.copy(selectedMarkets = v, marketsError = null) }
    fun onTrlSelected(v: String)              = _state.update { it.copy(selectedTrl = v, trlError = null) }

    fun save(onSuccess: () -> Unit) {
        if (!validate()) return
        screenModelScope.launch {
            _state.update { it.copy(isSubmitting = true, submitError = null) }
            val s = _state.value
            repository.updateTeam(
                UpdateTeamRequest(
                    teamId          = teamId,
                    name            = s.name.trim(),
                    meetingRoomLink = s.meetingRoomLink,
                    description     = s.description.trim(),
                    ntiMarketIds    = s.selectedMarkets.map { it.id },
                    readinessLevel  = s.selectedTrl,
                    trackerUsername = if (isAdmin) s.selectedTracker?.username else null,
                    streamId        = if (isAdmin) s.selectedStream?.id else null,
                )
            )
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(isSubmitting = false, submitError = "Ошибка при сохранении:\n${e.message}") }
                }
        }
    }

    fun deactivate(onSuccess: () -> Unit) {
        val trackerUsername = _state.value.originalTeam?.username ?: return
        screenModelScope.launch {
            _state.update { it.copy(isDeleting = true, deleteError = null) }
            repository.deleteTeam(teamId, trackerUsername)
                .onSuccess {
                    _state.update { it.copy(isDeleting = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(isDeleting = false, deleteError = "Ошибка деактивации:\n${e.message}") }
                }
        }
    }

    fun isLinkRegex(s: String): Boolean {
        val urlRegex = "^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".toRegex()
        return urlRegex.matches(s)
    }

    private fun validate(): Boolean {
        val s = _state.value
        val nameError = when {
            s.name.isBlank()    -> "Введите название"
            s.name.length < 2   -> "Минимум 2 символа"
            s.name.length > 100 -> "Максимум 100 символов"
            else                -> null
        }
        val meetingRoomLinkError = if (!isLinkRegex(s.meetingRoomLink) && s.meetingRoomLink.isNotBlank()) "Ссылка должна быть корректной (например https://webinar.tusur.ru/b/...)" else null
        val trackerError     = if (isAdmin && s.selectedTracker == null) "Выберите трекера" else null
        val streamError      = if (isAdmin && s.selectedStream == null) "Выберите поток" else null
        val marketsError     = if (s.selectedMarkets.isEmpty()) "Выберите хотя бы один рынок НТИ" else null
        val trlError         = if (s.selectedTrl.isBlank()) "Выберите TRL" else null
        val descriptionError = when {
            s.description.isBlank()     -> "Добавьте описание"
            s.description.length < 10   -> "Минимум 10 символов"
            s.description.length > 2000 -> "Максимум 2000 символов"
            else                        -> null
        }
        _state.update {
            it.copy(
                nameError = nameError,
                meetingRoomLinkError = meetingRoomLinkError,
                trackerError = trackerError,
                streamError = streamError,
                marketsError = marketsError,
                trlError = trlError,
                descriptionError = descriptionError
            )
        }
        return listOf(nameError, meetingRoomLinkError, trackerError, streamError, marketsError, trlError, descriptionError).all { it == null }
    }
}