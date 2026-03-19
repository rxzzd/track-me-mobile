package com.example.track_me_mobile.features.reports.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(TrackMePurple)
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(thumbHeightDp)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        translationY = (trackHeightPx - thumbHeightDp.toPx()) * scrollFraction
                    }
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(TrackMePurple)
            )
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(thumbWidthDp)
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        translationX = (trackWidthPx - thumbWidthDp.toPx()) * scrollFraction
                    }
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun FilterDropdownMenu(
    title: String,
    current: String,
    items: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val displayText = if (current == "Все") title else current

    Box(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { expanded = !expanded }
                .border(
                    2.dp, TrackMePurple,
                    if (expanded) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    else RoundedCornerShape(50.dp)
                ),
            shape = if (expanded) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            else RoundedCornerShape(50.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    displayText,
                    color = TrackMePurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = TrackMePurple
                )
            }
        }

        if (expanded) {
            Popup(
                offset = androidx.compose.ui.unit.IntOffset(
                    0,
                    with(androidx.compose.ui.platform.LocalDensity.current) {
                        48.dp.roundToPx()
                    }
                ),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnClickOutside = true
                )
            ) {
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { isVisible = true }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .background(
                                Color.White,
                                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                            )
                            .border(
                                2.dp,
                                TrackMePurple,
                                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                            )
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(scrollState)
                            ) {
                                items.forEachIndexed { i, item ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (i % 2 == 0) Color.White
                                                else Color(0xFFE8E0FF)
                                            )
                                            .clickable {
                                                onSelect(item)
                                                expanded = false
                                            }
                                            .padding(16.dp, 12.dp)
                                    ) {
                                        Text(item, fontSize = 13.sp, color = Color.Black)
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .width(24.dp)
                                    .fillMaxHeight()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    null,
                                    tint = TrackMePurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                CustomVerticalScrollbar(
                                    scrollState,
                                    Modifier.weight(1f).padding(vertical = 2.dp)
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    null,
                                    tint = TrackMePurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}