package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MarketGrid(
    selectedMarkets: Set<String>,
    onMarketToggle: (String) -> Unit
) {
    val markets = listOf(
        "AutoNet", "MariNet", "SafeNet", "TechNet",
        "HealthNet", "NeuroNet", "FoodNet", "WearNet"
    )
    val halfSize = (markets.size + 1) / 2

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(83.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            for (i in 0 until halfSize) {
                val market = markets[i]
                FilterOption(
                    text = market,
                    isSelected = market in selectedMarkets,
                    onCheckedChange = { onMarketToggle(market) }
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            for (i in halfSize until markets.size) {
                val market = markets[i]
                FilterOption(
                    text = market,
                    isSelected = market in selectedMarkets,
                    onCheckedChange = { onMarketToggle(market) }
                )
            }
        }
    }
}