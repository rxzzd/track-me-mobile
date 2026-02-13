package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import trackmemobile.composeapp.generated.resources.Res
import trackmemobile.composeapp.generated.resources.search_icon
import org.jetbrains.compose.resources.Font
import trackmemobile.composeapp.generated.resources.Mulish_Regular

@Composable
fun SearchBar(
    modifier: Modifier = Modifier
) {
    val mulishFamily = FontFamily(
        Font(Res.font.Mulish_Regular, FontWeight.Normal)
    )
    var text by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .width(244.dp)
            .height(40.dp)
            .shadow(
                elevation = 2.dp,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000),
                shape = RoundedCornerShape(40.dp)
            )
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFFCDAFF7))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.search_icon),
                contentDescription = "Search",
                tint = Color(0xFF44069A),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(17.dp))

            if (text.isEmpty()) {
                Text(
                    text = "Найти",
                    fontFamily = mulishFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    if (text.isNotEmpty()) {
                        innerTextField()
                    }
                },
                singleLine = true
            )
        }
    }
}