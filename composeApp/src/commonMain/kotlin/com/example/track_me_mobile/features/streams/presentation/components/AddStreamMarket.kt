package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.track_me_mobile.features.streams.presentation.AddStreamViewModel
import org.jetbrains.compose.resources.Font
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.Mulish_SemiBold

@Composable
fun AddStreamMarket(
    viewModel: AddStreamViewModel,
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
            containerColor = Color(0xFFE6D7FB)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 20.dp),
        ) {
            Text(
                text = "Рынки НТИ",
                fontSize = 20.sp,
                color = Color(0xFF4E13A0),
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
                fontFamily = mulishFamily,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(23.dp))

            // Передаем динамические данные в сетку
            MarketGrid(
                availableMarkets = viewModel.availableMarkets,
                selectedMarketIds = viewModel.selectedMarketIds,
                onMarketToggle = { id -> viewModel.toggleMarket(id) },
                borderColor = Color(0xFF4E13A0)
            )
        }
    }
}