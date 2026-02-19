package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.example.track_me_mobile.core.ui.components.MainTopHeader
// Импортируем ваш компонент хедера.

import com.example.track_me_mobile.features.streams.presentation.components.*

class StreamListScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<StreamListViewModel>()
        StreamListContent(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreamListContent(viewModel: StreamListViewModel) {

    val filterInfoBlockBounds = remember { mutableStateOf<Rect?>(null) }
    var showFilterPopUp by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            // Подгружаем, когда осталось 3 элемента до конца
            lastVisible >= total - 3 && !viewModel.isLoadingMore && viewModel.hasMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    MaterialTheme {
        Scaffold(
            // ── ВСТАВЛЯЕМ ВАШ ХЕДЕР ЗДЕСЬ ──
            topBar = {
                MainTopHeader()
            },
            // Задаем цвет фона для всего Scaffold, чтобы избежать белых полос при оттягивании списка
            containerColor = Color(0xFFF8F3FF)
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Учитываем высоту хедера автоматически
                    .background(Color(0xFFF8F3FF))
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Поисковая строка + кнопки
                    item(key = "search_bar") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.width(328.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterBtn(
                                modifier = Modifier.width(49.dp),
                                onClick = { showFilterPopUp = true }
                            )
                            SearchBar(
                                modifier = Modifier.width(244.dp),
                                value = viewModel.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                onSearch = { viewModel.applyFilters() }
                            )
                            AddBtn(modifier = Modifier.width(24.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // ── Блоки активных фильтров
                    item(key = "filter_info") {
                        Row(
                            modifier = Modifier
                                .width(328.dp)
                                .onGloballyPositioned { coordinates ->
                                    filterInfoBlockBounds.value = coordinates.boundsInRoot()
                                },
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterInfoBlock("Год(${viewModel.selectedYears.size})")
                            FilterInfoBlock("Рынки(${viewModel.selectedMarkets.size})")
                            FilterInfoBlock("TRL(${viewModel.selectedTrls.size})")
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // ── Состояние загрузки (первая загрузка)
                    if (viewModel.isLoading) {
                        item(key = "loading_indicator") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF8338EB))
                            }
                        }
                    }

                    // ── Ошибка
                    viewModel.errorMessage?.let { error ->
                        item(key = "error_message") {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // ── Список карточек
                    itemsIndexed(
                        items = viewModel.streams,
                        key = { index, stream -> "${index}_${stream.id}" }
                    ) { _, stream ->
                        StreamCard(
                            title = stream.name,
                            markets = "Рынки НТИ: ${stream.ntiMarkets.joinToString { it.displayName }}",
                            trl = "Дата начала: ${stream.startDate}",
                            flow = "Дата конца: ${stream.endDate}"
                        )
                    }

                    // ── Лоадер внизу при подгрузке следующей страницы
                    if (viewModel.isLoadingMore) {
                        item(key = "loading_more_indicator") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF8338EB),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // ── Попап фильтров
                FilterPopUp(
                    showWindow = showFilterPopUp,
                    onDismiss = { showFilterPopUp = false },
                    anchorBounds = filterInfoBlockBounds.value,
                    verticalOffset = (-24).dp,
                    availableMarkets = viewModel.availableMarkets,
                    selectedYears = viewModel.selectedYears,
                    selectedMarkets = viewModel.selectedMarkets,
                    selectedTrls = viewModel.selectedTrls,
                    onYearToggle = { viewModel.onYearToggle(it) },
                    onMarketToggle = { viewModel.onMarketToggle(it) },
                    onTrlToggle = { viewModel.onTrlToggle(it) },
                    onApply = { viewModel.applyFilters() },
                    onReset = { viewModel.resetFilters() }
                )
            }
        }
    }
}