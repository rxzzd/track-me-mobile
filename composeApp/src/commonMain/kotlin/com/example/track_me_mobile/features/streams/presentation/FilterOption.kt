package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import trackmemobile.composeapp.generated.resources.Mulish_SemiBold
import trackmemobile.composeapp.generated.resources.Res
import trackmemobile.composeapp.generated.resources.tick_icon

@Composable
fun FilterOption(
    text: String,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val mulishFamily = FontFamily(
        Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold)
    )
    Row(
        modifier = modifier
            .wrapContentWidth()
            .clickable { onCheckedChange(!isSelected) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier
                .size(17.dp)
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(2.dp)
                ).wrapContentSize(Alignment.Center, unbounded = true)
        ) { //исправить галочку
            if (isSelected) {
                Icon(
                    painter = painterResource(Res.drawable.tick_icon),
                    contentDescription = "Checked",
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center)
                )
            }
        }

        Text(
            text = text,
            fontSize = 13.sp,
            fontFamily = mulishFamily,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            lineHeight = 13.sp,
            letterSpacing = 0.sp,
            maxLines = 1
        )
    }
}