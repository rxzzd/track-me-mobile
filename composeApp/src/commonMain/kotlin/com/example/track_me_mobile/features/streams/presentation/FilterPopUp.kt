package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import trackmemobile.composeapp.generated.resources.Mulish_SemiBold
import trackmemobile.composeapp.generated.resources.Res
import trackmemobile.composeapp.generated.resources.close_icon


@Composable
fun FilterPopUp(
    showWindow: Boolean,
    onDismiss: () -> Unit,
    anchorBounds: Rect? = null,
    verticalOffset: Dp = 20.dp,
    previewMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (previewMode) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (anchorBounds != null) {
                val density = LocalDensity.current
                val offsetY = with(density) {
                    anchorBounds.bottom.toDp() + verticalOffset
                }

                FilterCardContent(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = offsetY)
                )
            } else {
                FilterCardContent(
                    modifier = modifier.align(Alignment.TopCenter)
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (showWindow) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onDismiss() }
                )

                Popup(
                    alignment = Alignment.TopCenter,
                    properties = PopupProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        focusable = true,
                    )
                ) {
                    if (anchorBounds != null) {
                        val density = LocalDensity.current
                        val offsetY = with(density) {
                            anchorBounds.bottom.toDp() + verticalOffset
                        }

                        Box(
                            modifier = Modifier.offset(y = offsetY)
                        ) {
                            FilterCardContent()
                        }
                    } else {
                        FilterCardContent()
                    }
                }
            }
        }
    }
}

@Composable
fun FilterCardContent(
    modifier: Modifier = Modifier
) {
    val mulishFamily = FontFamily(
        Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold)
    )

    Card(
        modifier = modifier
            .width(328.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFCDAFF7)
        )
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
                    .padding(vertical = 20.dp),
            ) {
                Column {
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.wrapContentSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(46.dp, Alignment.Start)
                        ) {
                            FilterOption(
                                "2016",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "2017",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "2018",
                                isSelected = false,
                                onCheckedChange = {})
                        }
                        Row(
                            modifier = Modifier.wrapContentSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(46.dp, Alignment.Start)
                        ) {
                            FilterOption(
                                "2019",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "2020",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "2021",
                                isSelected = false,
                                onCheckedChange = {})
                        }
                        Row(
                            modifier = Modifier.wrapContentSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(46.dp, Alignment.Start)
                        ) {
                            FilterOption(
                                "2022",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "2023",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "2024",
                                isSelected = false,
                                onCheckedChange = {})
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(83.dp, Alignment.Start)
                    ) {
                        Column(
                            modifier = Modifier.wrapContentSize(),
                            verticalArrangement = Arrangement.spacedBy(15.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            FilterOption(
                                "AutoNet",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "MariNet",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "SafeNet",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "TechNet",
                                isSelected = false,
                                onCheckedChange = {})
                        }
                        Column(
                            modifier = Modifier.wrapContentSize(),
                            verticalArrangement = Arrangement.spacedBy(15.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            FilterOption(
                                "HealthNet",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "NeuroNet",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "FoodNet",
                                isSelected = false,
                                onCheckedChange = {})
                            FilterOption(
                                "WearNet",
                                isSelected = false,
                                onCheckedChange = {})
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

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

                    Column(
                        modifier = Modifier.wrapContentSize(),
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        FilterOption(
                            "0-2",
                            isSelected = false,
                            onCheckedChange = {})
                        FilterOption(
                            "3-5",
                            isSelected = false,
                            onCheckedChange = {})
                        FilterOption(
                            "6-8",
                            isSelected = false,
                            onCheckedChange = {})
                        FilterOption(
                            "9-10",
                            isSelected = false,
                            onCheckedChange = {})
                    }
                }
            }
            Icon(
                painter = painterResource(Res.drawable.close_icon),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp)
                    .padding(end = 22.dp)
                    .size(12.dp)
            )
        }
    }
}

@Preview
@Composable
fun FilterPopUpPreview() {
    FilterCardContent()
}