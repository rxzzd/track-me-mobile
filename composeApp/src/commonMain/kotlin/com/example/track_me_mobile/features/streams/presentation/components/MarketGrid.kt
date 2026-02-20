package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
    if (availableMarkets.isEmpty()) return

    val halfSize = (availableMarkets.size + 1) / 2

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(83.dp) // Ваше старое расстояние
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            for (i in 0 until halfSize) {
                val market = availableMarkets[i]
                FilterOption(
                    text = market,
                    isSelected = market in selectedMarkets,
                    onCheckedChange = { onMarketToggle(market) },
                    borderColor = borderColor
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            for (i in halfSize until availableMarkets.size) {
                val market = availableMarkets[i]
                FilterOption(
                    text = market,
                    isSelected = market in selectedMarkets,
                    onCheckedChange = { onMarketToggle(market) },
                    borderColor = borderColor
                )
            }
        }
    }
}