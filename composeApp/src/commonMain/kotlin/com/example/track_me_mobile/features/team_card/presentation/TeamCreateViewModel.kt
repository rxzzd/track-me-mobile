package com.example.track_me_mobile.features.team_card.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
import com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest
import com.example.track_me_mobile.features.team_card.domain.models.TrackerUser
import com.example.track_me_mobile.features.teams.domain.models.NtiMarket
import com.example.track_me_mobile.features.teams.domain.models.Stream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateTeamUiState(
    val isLoading: Boolean = true,
    val loadError: String? = null,

    val trackers: List<TrackerUser> = emptyList(),
    val streams: List<Stream> = emptyList(),
    val ntiMarkets: List<NtiMarket> = emptyList(),

    val teamName: String = "",
    val meetingRoomLink: String = "",
    val description: String = "",
    val selectedTracker: TrackerUser? = null,
    val selectedStream: Stream? = null,
    val selectedMarkets: List<NtiMarket> = emptyList(),
    val selectedTrl: String = "",

    val isTrackerRole: Boolean = false,

    val teamNameError: String? = null,
    val meetingRoomLinkError: String? = null,
    val trackerError: String? = null,
    val streamError: String? = null,
    val marketsError: String? = null,
    val trlError: String? = null,
    val descriptionError: String? = null,

    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val isSuccess: Boolean = false
)

val TRL_OPTIONS = listOf("0-2", "3-5", "6-8", "9-10")

class TeamCreateViewModel(
    private val repository: TeamCardRepository,
    private val userInfoHolder: UserInfoHolder
) : ScreenModel {

    private val _state = MutableStateFlow(CreateTeamUiState())
    val state: StateFlow<CreateTeamUiState> = _state.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, loadError = null) }

            val isTrackerRole = userInfoHolder.userInfo?.mainRole == Role.TRACKER
            val streamsResult = repository.getStreams()
            if (streamsResult.isFailure) {
                _state.update { it.copy(isLoading = false, loadError = "Не удалось загрузить потоки. Попробуйте ещё раз.") }
                return@launch
            }
            val marketsResult = repository.getNtiMarkets()
            if (marketsResult.isFailure) {
                _state.update { it.copy(isLoading = false, loadError = "Не удалось загрузить рынки НТИ. Попробуйте ещё раз.") }
                return@launch
            }
            val trackers: List<TrackerUser> = if (!isTrackerRole) {
                val trackersResult = repository.getTrackers()
                if (trackersResult.isFailure) {
                    _state.update { it.copy(isLoading = false, loadError = "Не удалось загрузить список трекеров. Попробуйте ещё раз.") }
                    return@launch
                }
                trackersResult.getOrElse { emptyList() }
            } else emptyList()

            val streams = streamsResult.getOrElse { emptyList() }
            val markets = marketsResult.getOrElse { emptyList() }
            val currentUserTracker: TrackerUser? = if (isTrackerRole) {
                userInfoHolder.userInfo?.let { info ->
                    TrackerUser(
                        id        = info.id,
                        username  = info.username,
                        fullName  = info.fullName ?: info.username,
                        email     = "",
                        avatarUrl = null
                    )
                }
            } else null

            _state.update {
                it.copy(
                    isLoading       = false,
                    trackers        = trackers,
                    streams         = streams,
                    ntiMarkets      = markets,
                    isTrackerRole   = isTrackerRole,
                    selectedTracker = currentUserTracker
                )
            }
        }
    }

    fun onTeamNameChange(value: String) = _state.update { it.copy(teamName = value, teamNameError = null) }
    fun onMeetingRoomChange(value: String) = _state.update {it.copy(meetingRoomLink = value, meetingRoomLinkError = null)}
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value, descriptionError = null) }
    fun onTrackerSelected(tracker: TrackerUser) = _state.update { it.copy(selectedTracker = tracker, trackerError = null) }
    fun onStreamSelected(stream: Stream) = _state.update { it.copy(selectedStream = stream, streamError = null) }
    fun onMarketsChanged(markets: List<NtiMarket>) = _state.update { it.copy(selectedMarkets = markets, marketsError = null) }
    fun onTrlSelected(trl: String) = _state.update { it.copy(selectedTrl = trl, trlError = null) }
    fun clearSubmitError() = _state.update { it.copy(submitError = null) }

    fun retry() = loadInitialData()

    fun submit(onSuccess: () -> Unit) {
        if (!validate()) return

        screenModelScope.launch {
            _state.update { it.copy(isSubmitting = true, submitError = null) }

            val s = _state.value
            val request = CreateTeamRequest(
                name            = s.teamName.trim(),
                description     = s.description.trim(),
                meetingRoomLink = s.meetingRoomLink.trim(),
                trackerUsername = s.selectedTracker!!.username,
                streamId        = s.selectedStream!!.id,
                ntiMarketIds    = s.selectedMarkets.map { it.id },
                readinessLevel  = s.selectedTrl
            )

            repository.createTeam(request)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false, isSuccess = true) }
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            submitError  = "Ошибка при создании команды:\n${e.message}"
                        )
                    }
                }
        }
    }

    fun isLinkRegex(s: String): Boolean {
        val urlRegex = "^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".toRegex()
        return urlRegex.matches(s)
    }

    private fun validate(): Boolean {
        val s = _state.value

        val teamNameError = when {
            s.teamName.isBlank()        -> "Введите название команды"
            s.teamName.length < 2       -> "Минимум 2 символа"
            s.teamName.length > 100     -> "Максимум 100 символов"
            else                        -> null
        }
        val meetingRoomLinkError = if (!isLinkRegex(s.meetingRoomLink) && s.meetingRoomLink.isNotBlank()) "Ссылка должна быть корректной (например https://webinar.tusur.ru/b/...)" else null
        val trackerError     = if (s.selectedTracker == null) "Выберите трекера" else null
        val streamError      = if (s.selectedStream == null) "Выберите поток" else null
        val marketsError     = if (s.selectedMarkets.isEmpty()) "Выберите хотя бы один рынок НТИ" else null
        val trlError         = if (s.selectedTrl.isBlank()) "Выберите TRL" else null
        val descriptionError = when {
            s.description.isBlank()      -> "Добавьте описание команды"
            s.description.length < 10    -> "Минимум 10 символов"
            s.description.length > 2000  -> "Максимум 2000 символов"
            else                         -> null
        }

        _state.update {
            it.copy(
                teamNameError    = teamNameError,
                meetingRoomLinkError = meetingRoomLinkError,
                trackerError     = trackerError,
                streamError      = streamError,
                marketsError     = marketsError,
                trlError         = trlError,
                descriptionError = descriptionError
            )
        }

        return listOf(teamNameError, meetingRoomLinkError, trackerError, streamError, marketsError, trlError, descriptionError).all { it == null }
    }
}