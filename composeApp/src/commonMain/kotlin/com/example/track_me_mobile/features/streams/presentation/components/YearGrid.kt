package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun YearGrid(
    selectedYears: Set<String>,
    onYearToggle: (String) -> Unit
) {
    val years = remember {
        (2016..2026).map { it.toString() }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(46.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        items(years.size) { index ->
            val year = years[index]
            FilterOption(
                text = year,
                isSelected = year in selectedYears,
                onCheckedChange = { onYearToggle(year) },
                borderColor = Color.White
            )
        }
    }
}