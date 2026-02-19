package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest
import kotlinx.coroutines.launch

class AddStreamViewModel(
    private val repository: StreamRepository
) : ScreenModel {

    var name by mutableStateOf("")
    var startDate by mutableStateOf("")
    var endDate by mutableStateOf("")

    // Для выбора рынков
    var availableMarkets by mutableStateOf<List<NtiMarket>>(emptyList())
    var selectedMarketIds by mutableStateOf<Set<String>>(emptySet())

    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadMarkets()
    }

    private fun loadMarkets() {
        screenModelScope.launch {
            repository.getNtiMarkets().onSuccess { availableMarkets = it }
        }
    }

    fun toggleMarket(id: String) {
        selectedMarketIds = if (id in selectedMarketIds) selectedMarketIds - id else selectedMarketIds + id
    }

    fun createStream(onSuccess: () -> Unit) {
        if (name.isBlank() || startDate.isBlank() || endDate.isBlank()) {
            errorMessage = "Заполните обязательные поля"
            return
        }

        screenModelScope.launch {
            isLoading = true
            errorMessage = null

            val request = StreamCreateRequest(
                name = name,
                startDate = startDate,
                endDate = endDate,
                ntiMarketIds = selectedMarketIds.toList(),
                description = "Новый поток",
                trackStartDate = startDate,
                meetingsCount = 0
            )

            repository.createStream(request)
                .onSuccess {
                    isSuccess = true
                    onSuccess()
                }
                .onFailure {
                    errorMessage = it.message ?: "Ошибка при создании"
                }
            isLoading = false
        }
    }
}