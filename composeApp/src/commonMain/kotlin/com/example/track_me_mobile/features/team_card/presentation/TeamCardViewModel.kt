package com.example.track_me_mobile.features.team_card.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
import kotlinx.coroutines.launch

sealed class TeamCardState {
    object Loading : TeamCardState()
    data class Success(
        val team: TeamCard,
        val trackerFullName: String
    ) : TeamCardState()
    data class Error(val message: String) : TeamCardState()
}

class TeamCardViewModel(
    private val teamId: String,
    private val repository: TeamCardRepository
) : ScreenModel {

    var state by mutableStateOf<TeamCardState>(TeamCardState.Loading)
        private set

    init {
        println("[TEAM_CARD_VM] init with teamId=$teamId")
        loadTeam()
    }

    fun loadTeam() {
        screenModelScope.launch {
            println("[TEAM_CARD_VM] loadTeam: start, teamId=$teamId")
            state = TeamCardState.Loading
            repository.getTeamById(teamId)
                .onSuccess { team ->
                    println("[TEAM_CARD_VM] loadTeam: team loaded, id=${team.id}, username=${team.username}")
                    val fullNameResult = repository.getTrackerFullName(team.username)
                    val fullName = fullNameResult.getOrElse {
                        println("[TEAM_CARD_VM] loadTeam: getTrackerFullName failed: ${it.message}")
                        null
                    }
                    val resolvedName = fullName ?: "ФИО недоступно"
                    println("[TEAM_CARD_VM] loadTeam: tracker full name resolved to '$resolvedName'")
                    state = TeamCardState.Success(team, resolvedName)
                }
                .onFailure { e ->
                    println("[TEAM_CARD_VM] loadTeam: failed, error=${e.message}")
                    state = TeamCardState.Error("Не удалось загрузить карточку команды")
                }
        }
    }
}