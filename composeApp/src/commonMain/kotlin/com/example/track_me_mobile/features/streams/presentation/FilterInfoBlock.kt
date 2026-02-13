package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import trackmemobile.composeapp.generated.resources.Mulish_SemiBold
import trackmemobile.composeapp.generated.resources.Res

@Composable
fun FilterInfoBlock(
    text: String = "Текст",
    modifier: Modifier = Modifier
) {
    val mulishFamily = FontFamily(
        Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold)
    )
    Box(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF8338EB))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.sp,
            maxLines = 1,
            fontFamily = mulishFamily,
            fontWeight = FontWeight.SemiBold
        )
    }
}