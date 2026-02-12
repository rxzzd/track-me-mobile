package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FilterBtn(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .width(49.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF8338EB))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // add icon
    }
}