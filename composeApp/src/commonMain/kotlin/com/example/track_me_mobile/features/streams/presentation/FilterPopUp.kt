package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import trackmemobile.composeapp.generated.resources.Mulish_SemiBold
import trackmemobile.composeapp.generated.resources.Montserrat_Bold
import trackmemobile.composeapp.generated.resources.Res
import trackmemobile.composeapp.generated.resources.close_icon


@Composable
fun FilterPopUp(
    showWindow: Boolean,
    onDismiss: () -> Unit,
    anchorBounds: Rect? = null,
    verticalOffset: Dp = 0.dp,
    modifier: Modifier = Modifier
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
            )
            {
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
                        FilterCardContent(onDismiss = onDismiss)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterCardContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val mulishFamily = FontFamily(
        Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold)
    )
    val montserratFamily = FontFamily(
        Font(Res.font.Montserrat_Bold, FontWeight.Bold)
    )
    Card(
        modifier = modifier
            .width(328.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .clickable { },
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

                    YearGrid()

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

                    MarketGrid()

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

                    TrlGrid()
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp)
                    .padding(end = 22.dp)
                    .size(12.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.close_icon),
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }

            Text(
                text = "Сбросить", fontSize = 14.sp,
                color = Color.White,
                lineHeight = 14.sp,
                letterSpacing = 0.sp,
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp)
                    .padding(bottom = 46.dp)
            )

            Text(
                text = "Применить", fontSize = 14.sp,
                color = Color.White,
                lineHeight = 14.sp,
                letterSpacing = 0.sp,
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp)
                    .padding(bottom = 20.dp)
            )
        }
    }
}