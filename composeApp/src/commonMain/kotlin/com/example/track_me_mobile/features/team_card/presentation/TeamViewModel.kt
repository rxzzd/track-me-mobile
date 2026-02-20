package com.example.track_me_mobile.features.team_card.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TeamViewModel : ViewModel() {

    private val _teamData = MutableStateFlow(TeamFilterData())
    val teamData: StateFlow<TeamFilterData> = _teamData.asStateFlow()

    fun updateTrackerName(name: String) {
        _teamData.update { it.copy(trackerName = name) }
    }

    fun updateStream(stream: String) {
        _teamData.update { it.copy(stream = stream) }
    }

    fun updateMarkets(markets: List<String>) {
        _teamData.update { it.copy(markets = markets) }
    }

    fun updateTrl(trl: String) {
        _teamData.update { it.copy(trl = trl) }
    }

    fun updateDescription(description: String) {
        _teamData.update { it.copy(description = description) }
    }

    fun applyEdit(updated: TeamFilterData) {
        _teamData.value = updated
    }

    fun reset() {
        _teamData.value = TeamFilterData()
    }
}