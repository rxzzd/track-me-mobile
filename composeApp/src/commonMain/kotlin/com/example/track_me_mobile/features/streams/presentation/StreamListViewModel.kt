package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.Stream
import com.example.track_me_mobile.features.streams.domain.models.StreamFilter
import kotlinx.coroutines.launch

class StreamListViewModel(
    private val repository: StreamRepository
) : ScreenModel {

    // ─── UI State ───────────────────────────────────────────────────────────
    private var allStreams by mutableStateOf<List<Stream>>(emptyList())

    var streams by mutableStateOf<List<Stream>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var hasMore by mutableStateOf(true)
        private set

    // ─── Фильтры ────────────────────────────────────────────────────────────
    var searchQuery by mutableStateOf("")
        private set

    var selectedYears by mutableStateOf<Set<String>>(emptySet())
        private set

    var selectedMarkets by mutableStateOf<Set<String>>(emptySet())
        private set

    var selectedTrls by mutableStateOf<Set<String>>(emptySet())
        private set

    var availableMarkets by mutableStateOf<List<String>>(emptyList())
        private set

    private var marketNameMap by mutableStateOf<Map<String, String>>(emptyMap())

    // ─── Пагинация ──────────────────────────────────────────────────────────
    private var currentPage = 0
    private val pageSize = 10

    init {
        loadStreams(reset = true)
        loadMarkets()
    }

    // ─── Публичные методы ────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        applyLocalFilters()
    }

    fun onYearsChange(years: Set<String>) {
        selectedYears = years
    }

    fun onMarketsChange(markets: Set<String>) {
        selectedMarkets = markets
    }

    fun onTrlsChange(trls: Set<String>) {
        selectedTrls = trls
    }

    fun onYearToggle(year: String) {
        selectedYears = if (year in selectedYears)
            selectedYears - year else selectedYears + year
    }

    fun onMarketToggle(market: String) {
        selectedMarkets = if (market in selectedMarkets)
            selectedMarkets - market else selectedMarkets + market
    }

    fun onTrlToggle(trl: String) {
        selectedTrls = if (trl in selectedTrls)
            selectedTrls - trl else selectedTrls + trl
    }

    /** Применить фильтры — перезагружаем с API */
    fun applyFilters() {
        loadStreams(reset = true)
    }

    /** Сбросить все фильтры */
    fun resetFilters() {
        searchQuery = ""
        selectedYears = emptySet()
        selectedMarkets = emptySet()
        selectedTrls = emptySet()
        loadStreams(reset = true)
    }

    /** Бесконечный скролл — грузим следующую страницу */
    fun loadNextPage() {
        if (isLoadingMore || !hasMore) return
        loadStreams(reset = false)
    }

    // ─── Приватная логика ────────────────────────────────────────────────────

    private fun loadStreams(reset: Boolean) {
        screenModelScope.launch {
            if (reset) {
                allStreams = emptyList()
                streams = emptyList()
                isLoading = true
                errorMessage = null
                currentPage = 0
                hasMore = true
            } else {
                isLoadingMore = true
            }

            val filters = buildFilters()

            repository.getStreams(
                filters = filters,
                page = currentPage,
                size = pageSize
            ).onSuccess { page ->
                val newList = if (reset) page.content else allStreams + page.content
                allStreams = newList.distinctBy { it.id }
                applyLocalFilters()
                hasMore = currentPage + 1 < page.totalPages
                currentPage++
            }.onFailure {
                errorMessage = "Не удалось загрузить потоки"
            }

            isLoading = false
            isLoadingMore = false
        }
    }

    private fun applyLocalFilters() {
        streams = if (searchQuery.isBlank()) {
            allStreams
        } else {
            allStreams.filter { stream ->
                stream.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    private fun buildFilters(): List<StreamFilter> {
        val result = mutableListOf<StreamFilter>()

        selectedYears.forEach { year ->
            result += StreamFilter(fieldName = "year", type = "EQ", value = year)
        }
        selectedMarkets.forEach { displayName ->
            val internalName = marketNameMap[displayName] ?: displayName
            result += StreamFilter(fieldName = "ntiMarkets.name", type = "EQ", value = internalName)
        }
        selectedTrls.forEach { trl ->
            result += StreamFilter(fieldName = "teamCards.readinessLevel", type = "EQ", value = trl)
        }

        return result
    }

    private fun loadMarkets() {
        screenModelScope.launch {
            repository.getNtiMarkets()
                .onSuccess { markets ->
                    availableMarkets = markets.map { it.displayName }.sorted()
                    marketNameMap = markets.associate { it.displayName to it.name }
                    println("### NTI_MARKETS loaded: ${availableMarkets.size} markets")
                }
                .onFailure {
                    println("### NTI_MARKETS failed to load")
                }
        }
    }
}