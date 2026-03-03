package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.TrackMePurple

@Composable
fun TableCell(text: String, width: Dp, isHeader: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.width(width).heightIn(min = 44.dp).border(0.5.dp, TrackMePurple).padding(8.dp),
        fontSize = 12.sp, fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center, color = Color.Black
    )
}

@Composable
fun CustomVerticalScrollbar(scrollState: ScrollState, modifier: Modifier = Modifier) {
    if (scrollState.maxValue > 0) {
        val scrollFraction = scrollState.value.toFloat() / scrollState.maxValue
        BoxWithConstraints(modifier = modifier.width(6.dp).fillMaxHeight()) {
            val trackHeightPx = constraints.maxHeight.toFloat()
            val thumbHeightDp = 40.dp
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(50)).background(TrackMePurple))
            Box(
                modifier = Modifier.width(4.dp).height(thumbHeightDp).align(Alignment.TopCenter)
                    .graphicsLayer { translationY = (trackHeightPx - thumbHeightDp.toPx()) * scrollFraction }
                    .padding(vertical = 2.dp).clip(RoundedCornerShape(50)).background(Color.White)
            )
        }
    }
}

@Composable
fun CustomHorizontalScrollbar(scrollState: ScrollState, modifier: Modifier = Modifier) {
    if (scrollState.maxValue > 0) {
        val scrollFraction = scrollState.value.toFloat() / scrollState.maxValue
        BoxWithConstraints(modifier = modifier.height(6.dp).fillMaxWidth()) {
            val trackWidthPx = constraints.maxWidth.toFloat()
            val thumbWidthDp = 80.dp
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(50)).background(TrackMePurple))
            Box(
                modifier = Modifier.height(4.dp).width(thumbWidthDp).align(Alignment.CenterStart)
                    .graphicsLayer { translationX = (trackWidthPx - thumbWidthDp.toPx()) * scrollFraction }
                    .padding(horizontal = 2.dp).clip(RoundedCornerShape(50)).background(Color.White)
            )
        }
    }
}