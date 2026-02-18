package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrlGrid(
    selectedTrls: Set<String>,
    onTrlToggle: (String) -> Unit
) {
    val trls = listOf("0-2", "3-5", "6-8", "9-10")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.Start
    ) {
        trls.forEach { trl ->
            FilterOption(
                text = trl,
                isSelected = trl in selectedTrls,
                onCheckedChange = { onTrlToggle(trl) }
            )
        }
    }
}