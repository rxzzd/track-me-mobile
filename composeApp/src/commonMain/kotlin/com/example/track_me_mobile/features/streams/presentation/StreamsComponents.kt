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
fun TableCell(
    text: String,
    width: Dp,
    isHeader: Boolean = false,
    textColor: Color = Color.Black
) {
    val bg = if (isHeader) Color(0xFFD1C4E9) else Color.Transparent
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .defaultMinSize(minHeight = 44.dp)
            .background(bg)
            .border(0.5.dp, TrackMePurple)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 11.sp else 12.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = textColor,
            lineHeight = 14.sp
        )
    }
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