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

interface StreamMarketState {
    var availableMarkets: List<NtiMarket>
    var selectedMarketIds: Set<String>
    fun toggleMarket(id: String)
}

class AddStreamViewModel(
    private val repository: StreamRepository
) : ScreenModel, StreamMarketState {

    var name by mutableStateOf("")
    var startDate by mutableStateOf("")
    var endDate by mutableStateOf("")
    var trackStartDate by mutableStateOf("") // НОВОЕ ПОЛЕ
    var meetingsCount by mutableStateOf(0)   // НОВОЕ ПОЛЕ

    override var availableMarkets by mutableStateOf<List<NtiMarket>>(emptyList())
    override var selectedMarketIds by mutableStateOf<Set<String>>(emptySet())

    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var pendingImageBytes by mutableStateOf<ByteArray?>(null)
        private set

    var isUploadingImage by mutableStateOf(false)
        private set

    init {
        loadMarkets()
    }

    private fun loadMarkets() {
        screenModelScope.launch {
            println("### VM_DEBUG: Начинаю загрузку рынков...")
            repository.getNtiMarkets()
                .onSuccess {
                    println("### VM_DEBUG: Успешно получено ${it.size} рынков")
                    availableMarkets = it
                }
                .onFailure {
                    println("### VM_DEBUG: ОШИБКА загрузки: ${it.message}")
                    errorMessage = it.message
                }
        }
    }

    override fun toggleMarket(id: String) {
        selectedMarketIds = if (id in selectedMarketIds) selectedMarketIds - id else selectedMarketIds + id
    }

    fun createStream(onSuccess: () -> Unit) {
        if (name.isBlank() || startDate.isBlank() || endDate.isBlank() || trackStartDate.isBlank()) {
            errorMessage = "Заполните обязательные поля"
            return
        }

        if (meetingsCount == 0) {
            errorMessage = "Выберите количество встреч"
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
                trackStartDate = trackStartDate,
                meetingsCount = meetingsCount
            )

            repository.createStream(request)
                .onSuccess { createdStream ->
                    // Если есть фото - загружаем его
                    if (pendingImageBytes != null) {
                        uploadImageForCreatedStream(createdStream.id, onSuccess)
                    } else {
                        isSuccess = true
                        isLoading = false
                        onSuccess()
                    }
                }
                .onFailure {
                    errorMessage = it.message ?: "Ошибка при создании"
                    isLoading = false
                }
        }
    }

    private fun uploadImageForCreatedStream(streamId: String, onSuccess: () -> Unit) {
        val bytes = pendingImageBytes ?: return

        screenModelScope.launch {
            isUploadingImage = true

            repository.uploadStreamImage(streamId, bytes)
                .onSuccess {
                    isSuccess = true
                    pendingImageBytes = null
                    onSuccess()
                }
                .onFailure {
                    errorMessage = "Поток создан, но не удалось загрузить фото"
                }

            isUploadingImage = false
            isLoading = false
        }
    }

    fun setStreamImage(bytes: ByteArray) {
        pendingImageBytes = bytes
    }
}