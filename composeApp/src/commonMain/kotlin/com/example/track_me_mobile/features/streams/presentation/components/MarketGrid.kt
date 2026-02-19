package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MarketGrid(
    availableMarkets: List<String>,
    selectedMarkets: Set<String>,
    onMarketToggle: (String) -> Unit,
    borderColor: Color
) {
    if (availableMarkets.isEmpty()) {
        // Пока рынки грузятся — показываем плейсхолдер или ничего
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth().height(200.dp), // фиксированная высота чтобы не конфликтовать с родительским скроллом
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(availableMarkets.size) { index ->
            val market = availableMarkets[index]
            FilterOption(
                text = market,
                isSelected = market in selectedMarkets,
                onCheckedChange = { onMarketToggle(market) },
                borderColor = borderColor
            )
        }
    }
}