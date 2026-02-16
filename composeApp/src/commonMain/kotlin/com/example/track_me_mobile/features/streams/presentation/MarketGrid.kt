package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MarketGrid() {
    val markets = remember {
        listOf(
            "AutoNet", "MariNet", "SafeNet", "TechNet",
            "HealthNet", "NeuroNet", "FoodNet", "WearNet"
        )
    }
    val selectedStates = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(markets.size) { add(false) }
        }
    }
    val halfSize = (markets.size + 1) / 2

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(83.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            for (i in 0 until halfSize) {
                if (i < markets.size) {
                    FilterOption(
                        text = markets[i],
                        isSelected = selectedStates[i],
                        onCheckedChange = { isChecked ->
                            selectedStates[i] = isChecked
                        }
                    )
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            for (i in halfSize until markets.size) {
                FilterOption(
                    text = markets[i],
                    isSelected = selectedStates[i],
                    onCheckedChange = { isChecked ->
                        selectedStates[i] = isChecked
                    }
                )
            }
        }
    }
}