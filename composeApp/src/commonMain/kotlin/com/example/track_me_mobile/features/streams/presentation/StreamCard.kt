package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*

//import org.jetbrains.compose.resources.Font
//import trackmemobile.composeapp.generated.resources.Res
//import trackmemobile.composeapp.generated.resources.Montserrat_Regular
//import trackmemobile.composeapp.generated.resources.Montserrat_ExtraBold

@Composable
fun StreamCard(
    title: String = "Заголовок",
    markets: String = "Рынки НТИ:",
    trl: String = "TRL:",
    flow: String = "Поток:",
    modifier: Modifier = Modifier
) {
//    val montserratFamily = FontFamily(
//        Font(Res.font.Montserrat_Regular, FontWeight.Normal),
//        Font(Res.font.Montserrat_ExtraBold, FontWeight.ExtraBold)
//    )

    Card(
        modifier = modifier
//            .shadow(
//                elevation = 20.dp,  // ОДНА, НО ЯРКАЯ ТЕНЬ
//                spotColor = Color(0x807A00E5), // 50% прозрачности!
//                ambientColor = Color(0x807A00E5),
//                shape = RoundedCornerShape(20.dp)
//            )
            .width(328.dp)
            .height(165.dp)
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 148.dp, height = 125.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
//                Image(
//                    painter = painterResource(Res.drawable.your_image),
//                    contentDescription = null,
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Crop
//                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = TextPurple,
//                    fontFamily = montserratFamily,
                    fontWeight = FontWeight.ExtraBold,
//                    maxLines = 2,
                    lineHeight = 14.sp,
                    letterSpacing = 0.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = markets,
                    fontSize = 12.sp,
                    color = TextBlack,
//                    fontFamily = montserratFamily,
                    fontWeight = FontWeight.Normal,
//                    maxLines = 2,
                    lineHeight = 12.sp,
                    letterSpacing = 0.sp
                )

                Text(
                    text = trl,
                    fontSize = 12.sp,
                    color = TextBlack,
//                    fontFamily = montserratFamily,
                    fontWeight = FontWeight.Normal,
//                    maxLines = 2,
                    lineHeight = 12.sp,
                    letterSpacing = 0.sp
                )
                Text(
                    text = flow,
                    fontSize = 12.sp,
                    color = TextBlack,
//                    fontFamily = montserratFamily,
                    fontWeight = FontWeight.Normal,
//                    maxLines = 2,
                    lineHeight = 12.sp,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}