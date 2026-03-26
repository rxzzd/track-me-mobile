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

    private var allTeams: List<TeamCard> = emptyList()
    private var streamFilter: String? = null
    private var isInitialized = false

    var searchQuery by mutableStateOf("")
        private set

    fun initialize(streamId: String?) {
        if (!isInitialized) {
            streamFilter = streamId
            isInitialized = true
            loadTeams()
        }
    }

    fun setStreamFilter(streamId: String) {
        streamFilter = streamId
        loadTeams()
    }

    fun loadTeams() {
        screenModelScope.launch {
            state = TeamListState.Loading

            try {
                val pageSize = 100
                val accumulated = mutableListOf<TeamCard>()
                var currentPage = 0

                while (true) {
                    val result = repository.getTeamCards(streamId = streamFilter, page = currentPage, size = pageSize)

                    val pageTeams = result.getOrElse {
                        throw it
                    }

                    accumulated += pageTeams

                    if (pageTeams.size < pageSize) break
                    currentPage++
                }

                allTeams = accumulated
                applyFilters()
            } catch (e: Throwable) {
                println("[TEAMS_VM] Ошибка: ${e.message}")
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