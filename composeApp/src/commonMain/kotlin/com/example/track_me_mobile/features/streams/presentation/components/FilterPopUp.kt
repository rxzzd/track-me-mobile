package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import com.example.track_me_mobile.generated.resources.Mulish_SemiBold
import com.example.track_me_mobile.generated.resources.Montserrat_Bold
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.close_icon

@Composable
fun FilterPopUp(
    showWindow: Boolean,
    onDismiss: () -> Unit,
    anchorBounds: Rect? = null,
    verticalOffset: Dp = 0.dp,
    modifier: Modifier = Modifier,
    // Состояние фильтров
    selectedYears: Set<String> = emptySet(),
    selectedMarkets: Set<String> = emptySet(),
    selectedTrls: Set<String> = emptySet(),
    onYearToggle: (String) -> Unit = {},
    onMarketToggle: (String) -> Unit = {},
    onTrlToggle: (String) -> Unit = {},
    onApply: () -> Unit = {},
    onReset: () -> Unit = {}
) {
    if (showWindow) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                focusable = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                if (anchorBounds != null) {
                    val density = LocalDensity.current
                    val offsetY = with(density) {
                        anchorBounds.bottom.toDp() + verticalOffset
                    }
                    Box(
                        modifier = Modifier
                            .width(328.dp)
                            .padding(top = offsetY)
                            .wrapContentHeight()
                            .clickable { },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        FilterCardContent(
                            onDismiss = onDismiss,
                            selectedYears = selectedYears,
                            selectedMarkets = selectedMarkets,
                            selectedTrls = selectedTrls,
                            onYearToggle = onYearToggle,
                            onMarketToggle = onMarketToggle,
                            onTrlToggle = onTrlToggle,
                            onApply = {
                                onApply()
                                onDismiss()
                            },
                            onReset = {
                                onReset()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterCardContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    selectedYears: Set<String> = emptySet(),
    selectedMarkets: Set<String> = emptySet(),
    selectedTrls: Set<String> = emptySet(),
    onYearToggle: (String) -> Unit = {},
    onMarketToggle: (String) -> Unit = {},
    onTrlToggle: (String) -> Unit = {},
    onApply: () -> Unit = {},
    onReset: () -> Unit = {}
) {
    val mulishFamily = FontFamily(Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold))
    val montserratFamily = FontFamily(Font(Res.font.Montserrat_Bold, FontWeight.Bold))

    Card(
        modifier = modifier
            .width(328.dp)
            .height(560.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFCDAFF7))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(top = 20.dp)
                    .padding(bottom = 60.dp) // место под кнопки
            ) {
                // ── Год ──────────────────────────────────────────────────────
                Text(
                    text = "Год",
                    fontSize = 20.sp,
                    color = Color.White,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                    fontFamily = mulishFamily,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                YearGrid(
                    selectedYears = selectedYears,
                    onYearToggle = onYearToggle
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── Рынки НТИ ────────────────────────────────────────────────
                Text(
                    text = "Рынки НТИ",
                    fontSize = 20.sp,
                    color = Color.White,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                    fontFamily = mulishFamily,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                MarketGrid(
                    selectedMarkets = selectedMarkets,
                    onMarketToggle = onMarketToggle
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── TRL ──────────────────────────────────────────────────────
                Text(
                    text = "TRL",
                    fontSize = 20.sp,
                    color = Color.White,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                    fontFamily = mulishFamily,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                TrlGrid(
                    selectedTrls = selectedTrls,
                    onTrlToggle = onTrlToggle
                )
            }

            // ── Крестик закрытия ─────────────────────────────────────────────
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp, end = 22.dp)
                    .size(12.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.close_icon),
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }

            // ── Кнопки Сбросить / Применить ──────────────────────────────────
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Сбросить",
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 14.sp,
                    letterSpacing = 0.sp,
                    fontFamily = montserratFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onReset() }
                )
                Text(
                    text = "Применить",
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 14.sp,
                    letterSpacing = 0.sp,
                    fontFamily = montserratFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onApply() }
                )
            }
        }
    }
}