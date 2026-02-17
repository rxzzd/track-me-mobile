package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun YearGrid() {
    val years = remember { (2016..2024).map {it.toString()} }
    val selectedStates = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(years.size) { add(false) }
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(46.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        items(years.size) { index ->
            FilterOption(
                text = years[index],
                isSelected = selectedStates[index],
                onCheckedChange = { isChecked ->
                    selectedStates[index] = isChecked
                },
                borderColor = Color.White
            )
        }
    }
}