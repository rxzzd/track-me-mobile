package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.teams.domain.TeamRepository
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
import kotlinx.coroutines.launch

sealed class TeamListState {
    object Loading : TeamListState()
    data class Success(val teams: List<TeamCard>) : TeamListState()
    data class Error(val message: String) : TeamListState()
}

class TeamListViewModel(
    private val repository: TeamRepository
) : ScreenModel {

    var state by mutableStateOf<TeamListState>(TeamListState.Loading)
        private set

    // Оригинальный список с сервера — нужен для фильтрации без повторных запросов
    private var allTeams: List<TeamCard> = emptyList()

    var searchQuery by mutableStateOf("")
        private set

    init {
        loadTeams()
    }

    fun loadTeams() {
        screenModelScope.launch {
            state = TeamListState.Loading
            repository.getTeamCards()
                .onSuccess { teams ->
                    allTeams = teams
                    applyFilters()
                }
                .onFailure {
                    println("[TEAMS_VM] Ошибка: ${it.message}")
                    state = TeamListState.Error("Не удалось загрузить список команд")
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun onFilterApply(selectedMarkets: List<String>, selectedTrlRanges: List<IntRange>) {
        applyFilters(selectedMarkets, selectedTrlRanges)
    }

    private fun applyFilters(
        markets: List<String> = emptyList(),
        trlRanges: List<IntRange> = emptyList()
    ) {
        val filtered = allTeams.filter { team ->
            val matchesSearch = searchQuery.isBlank() ||
                    team.name.contains(searchQuery, ignoreCase = true) ||
                    team.description.contains(searchQuery, ignoreCase = true)

            val matchesMarket = markets.isEmpty() ||
                    team.ntiMarkets.any { market ->
                        markets.any { it.equals(market.displayName, ignoreCase = true) }
                    }

            val trlNumber = team.readinessLevel
                .substringBefore("-")
                .trim()
                .toIntOrNull() ?: 0

            val matchesTrl = trlRanges.isEmpty() ||
                    trlRanges.any { trlNumber in it }

            matchesSearch && matchesMarket && matchesTrl
        }

        state = TeamListState.Success(filtered)
    }
}