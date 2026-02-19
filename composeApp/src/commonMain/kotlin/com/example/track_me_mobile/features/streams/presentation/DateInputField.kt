package com.example.track_me_mobile.features.streams.presentation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import trackmemobile.composeapp.generated.resources.Mulish_SemiBold
import trackmemobile.composeapp.generated.resources.Res

@Composable
fun DateInputField(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit,
) {
    val mulishFamily = FontFamily(
        Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold)
    )
    val mask = "__.__.____"

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .width(120.dp)
            .height(40.dp)
            .background(
                color = Color(0xFFE6D7FB),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 24.dp, vertical = 11.dp),
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = Color(0xFF44069A),
            fontFamily = mulishFamily,
            fontWeight = FontWeight.SemiBold
        ),
        enabled = false,
        readOnly = true,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = mask,
                        color = Color(0xFF44069A),
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        }
    )
}