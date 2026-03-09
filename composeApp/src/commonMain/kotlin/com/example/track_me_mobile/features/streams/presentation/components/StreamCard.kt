package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.Montserrat_Regular
import com.example.track_me_mobile.generated.resources.Montserrat_ExtraBold
import com.example.track_me_mobile.generated.resources.Inter_28pt_Black
import com.example.track_me_mobile.generated.resources.base_stream_photo

@Composable
fun StreamCard(
    streamId: String = "",
    title: String = "Заголовок",
    markets: String = "Рынки НТИ:",
    trl: String = "TRL:",
    flow: String = "Поток:",
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val montserratFamily = FontFamily(
        Font(Res.font.Montserrat_Regular, FontWeight.Normal),
        Font(Res.font.Montserrat_ExtraBold, FontWeight.ExtraBold)
    )
    val interFamily = FontFamily(
        Font(Res.font.Inter_28pt_Black, FontWeight.Black)
    )

    Card(
        modifier = modifier
            .padding(bottom = 20.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true,
                ambientColor = Color(0xFF7A00E5),
                spotColor = Color(0xFF7A00E5)
            )
            .width(328.dp)
            .heightIn(min = 165.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(20.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    Image(
                        painter = painterResource(Res.drawable.base_stream_photo),
                        contentDescription = "Base photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        color = TextPurple,
                        fontFamily = montserratFamily,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        lineHeight = 16.sp,
                        letterSpacing = 0.sp,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = markets,
                        fontSize = 12.sp,
                        color = TextBlack,
                        fontFamily = montserratFamily,
                        fontWeight = FontWeight.Normal,
                        maxLines = 3,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = trl,
                        fontSize = 12.sp,
                        color = TextBlack,
                        fontFamily = montserratFamily,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        lineHeight = 14.sp
                    )

                    Text(
                        text = flow,
                        fontSize = 12.sp,
                        color = TextBlack,
                        fontFamily = montserratFamily,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(25.dp))
                }
            }

            Text(
                text = "Редактировать",
                fontSize = 12.sp,
                color = Color(0xFF8338EB),
                fontFamily = interFamily,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 13.dp, end = 20.dp)
                    .clickable(onClick = onEditClick)
            )
        }
    }
}