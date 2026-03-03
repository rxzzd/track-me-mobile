package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket

@Composable
fun MarketGrid(
    availableMarkets: List<NtiMarket>,
    selectedMarketIds: Set<String>,
    onMarketToggle: (String) -> Unit,
    borderColor: Color
) {
    if (availableMarkets.isEmpty()) return

    val halfSize = (availableMarkets.size + 1) / 2

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            for (i in 0 until halfSize) {
                val market = availableMarkets[i]
                FilterOption(
                    text = market.displayName, // Отображаем displayName из API
                    isSelected = market.id in selectedMarketIds,
                    onCheckedChange = { onMarketToggle(market.id) },
                    borderColor = borderColor
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            for (i in halfSize until availableMarkets.size) {
                val market = availableMarkets[i]
                FilterOption(
                    text = market.displayName,
                    isSelected = market.id in selectedMarketIds,
                    onCheckedChange = { onMarketToggle(market.id) },
                    borderColor = borderColor
                )
            }
        }
    }
}