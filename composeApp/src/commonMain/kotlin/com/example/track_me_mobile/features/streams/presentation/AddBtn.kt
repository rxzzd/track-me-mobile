package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.painterResource
import trackmemobile.composeapp.generated.resources.Res
import trackmemobile.composeapp.generated.resources.add_icon

@Composable
fun AddBtn(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .width(24.dp)
            .height(24.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.add_icon),
            contentDescription = "Add",
            tint = Color(0xFF8338EB),
            modifier = Modifier.size(20.dp)
        )
    }
}