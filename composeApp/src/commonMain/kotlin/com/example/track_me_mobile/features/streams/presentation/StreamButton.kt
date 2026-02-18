package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import trackmemobile.composeapp.generated.resources.Montserrat_ExtraBold
import trackmemobile.composeapp.generated.resources.Res

@Composable
fun StreamButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val montserratFamily = FontFamily(
        Font(Res.font.Montserrat_ExtraBold, FontWeight.ExtraBold)
    )
    Button(
        onClick = onClick,
        modifier = modifier
            .wrapContentWidth()
            .height(38.dp),
        enabled = isEnabled,
        shape = RoundedCornerShape(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF8338EB),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF8338EB),
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(
            horizontal = 60.dp,
            vertical = 11.dp
        )
    ) {
        Text(text = text,
            fontSize = 13.sp,
            lineHeight = 13.sp,
            letterSpacing = 0.sp,
            fontFamily = montserratFamily,
            fontWeight = FontWeight.ExtraBold
            )
    }
}