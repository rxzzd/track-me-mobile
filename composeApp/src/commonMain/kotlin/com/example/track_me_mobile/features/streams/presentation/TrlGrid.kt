package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrlGrid() {
    val trl = remember {
        listOf("0-2", "3-5", "6-8", "9-10")
    }

    val selectedStates = remember {
        mutableStateListOf<Boolean>().apply {
            repeat(trl.size) { add(false) }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.Start
    ) {
        trl.forEachIndexed { index, trl ->
            FilterOption(
                text = trl,
                isSelected = selectedStates[index],
                onCheckedChange = { isChecked ->
                    selectedStates[index] = isChecked
                }
            )
        }
    }
}