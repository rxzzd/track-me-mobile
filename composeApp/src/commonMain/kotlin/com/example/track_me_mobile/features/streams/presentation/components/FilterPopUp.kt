package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.track_me_mobile.generated.resources.*
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun FilterPopUp(
    showWindow: Boolean,
    onDismiss: () -> Unit,
    anchorBounds: Rect? = null,
    verticalOffset: Dp = 0.dp,
    modifier: Modifier = Modifier,
    availableMarkets: List<String> = emptyList(),
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
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { onDismiss() } },
                contentAlignment = Alignment.TopCenter
            ) {
                if (anchorBounds != null) {
                    val density = LocalDensity.current
                    val offsetY = with(density) { anchorBounds.bottom.toDp() + verticalOffset }

                    Box(
                        modifier = Modifier
                            .padding(top = offsetY)
                            .pointerInput(Unit) { detectTapGestures { } }
                    ) {
                        FilterCardContent(
                            onDismiss = onDismiss,
                            availableMarkets = availableMarkets,
                            selectedYears = selectedYears,
                            selectedMarkets = selectedMarkets,
                            selectedTrls = selectedTrls,
                            onYearToggle = onYearToggle,
                            onMarketToggle = onMarketToggle,
                            onTrlToggle = onTrlToggle,
                            onApply = { onApply(); onDismiss() },
                            onReset = { onReset(); onDismiss() }
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
    availableMarkets: List<String>,
    selectedYears: Set<String>,
    selectedMarkets: Set<String>,
    selectedTrls: Set<String>,
    onYearToggle: (String) -> Unit,
    onMarketToggle: (String) -> Unit,
    onTrlToggle: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    val mulishFamily = FontFamily(Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold))
    val montserratFamily = FontFamily(Font(Res.font.Montserrat_Bold, FontWeight.Bold))
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .width(328.dp)
            .heightIn(max = 600.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFCDAFF7))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 80.dp)
            ) {
                FilterSectionTitle("Год", mulishFamily)
                AlignedFilterGrid(
                    items = listOf("2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026"),
                    columns = 3,
                    selectedItems = selectedYears,
                    onToggle = onYearToggle,
                    fontFamily = mulishFamily
                )

                Spacer(modifier = Modifier.height(24.dp))

                FilterSectionTitle("Рынки НТИ", mulishFamily)
                AlignedFilterGrid(
                    items = availableMarkets,
                    columns = 2,
                    selectedItems = selectedMarkets,
                    onToggle = onMarketToggle,
                    fontFamily = mulishFamily
                )

                Spacer(modifier = Modifier.height(24.dp))

                FilterSectionTitle("TRL", mulishFamily)
                AlignedFilterGrid(
                    items = listOf("0-2", "3-5", "6-8", "9-10"),
                    columns = 3,
                    selectedItems = selectedTrls,
                    onToggle = onTrlToggle,
                    fontFamily = mulishFamily
                )
            }


            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(24.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.close_icon),
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth()
                    .background(Color(0xFFCDAFF7))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Сбросить",
                    color = Color.White,
                    fontFamily = montserratFamily,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onReset() }.padding(8.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Применить",
                    color = Color.White,
                    fontFamily = montserratFamily,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onApply() }.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun FilterSectionTitle(title: String, fontFamily: FontFamily) {
    Text(
        text = title,
        fontSize = 20.sp,
        color = Color.White,
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun AlignedFilterGrid(
    items: List<String>,
    columns: Int,
    selectedItems: Set<String>,
    onToggle: (String) -> Unit,
    fontFamily: FontFamily
) {
    val rows = items.chunked(columns)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    FilterItem(
                        label = item,
                        isSelected = selectedItems.contains(item),
                        onToggle = { onToggle(item) },
                        fontFamily = fontFamily,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FilterItem(
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(
                    width = 1.5.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    color = if (isSelected) Color.Black.copy(alpha = 0.1f) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    painter = painterResource(Res.drawable.tick_icon),
                    contentDescription = "Selected",
                    tint = Color.Black,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = label,
            color = Color.Black,
            fontSize = 14.sp,
            fontFamily = fontFamily,
            maxLines = 1,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun Dp.sp() = with(LocalDensity.current) { this@sp.toSp() }