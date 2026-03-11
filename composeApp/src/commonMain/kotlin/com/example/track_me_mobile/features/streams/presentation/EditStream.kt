package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.features.streams.presentation.components.AddStreamMarket
import com.example.track_me_mobile.features.streams.presentation.components.AddStreamPhoto
import com.example.track_me_mobile.features.streams.presentation.components.DateInputField
import com.example.track_me_mobile.features.streams.presentation.components.StreamButton
import com.example.track_me_mobile.features.streams.presentation.components.StreamNameInput
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import org.jetbrains.compose.resources.Font
import com.example.track_me_mobile.generated.resources.Mulish_SemiBold
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.edit
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun EditStreamPage() {
    MaterialTheme {
        var startDate by remember { mutableStateOf("") }
        var endDate by remember { mutableStateOf("") }
        val name = remember { mutableStateOf("") }
        val mulishFamily = FontFamily(
            Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold)
        )
        Scaffold(topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(Color(0xFF8338EB))
                    .padding(start = 18.dp, end = 18.dp)
            ) {
                //add logo and icon
            }
        }) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F3FF))
            ) {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Редактирование",
                        fontSize = 32.sp,
                        color = Color(0xFF44069A),
                        fontFamily = mulishFamily,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 32.sp,
                        letterSpacing = 0.sp
                    )
                    Text(
                        "потока",
                        fontSize = 32.sp,
                        color = Color(0xFF44069A),
                        fontFamily = mulishFamily,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 32.sp,
                        letterSpacing = 0.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AddStreamPhoto()

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .wrapContentHeight()
                            .width(328.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StreamNameInput(value = name.value, onValueChange = { name.value = it })
                        IconButton({}) {
                            Icon(
                                painter = painterResource(Res.drawable.edit),
                                contentDescription = "Edit stream",
                                tint = Color(0xFF44069A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .wrapContentHeight()
                            .width(328.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .width(220.dp)
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Начало:",
                                color = Color(0xFF8338EB),
                                fontFamily = mulishFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                letterSpacing = 0.sp
                            )
                            DateInputField(value = startDate, onValueChange = { startDate = it })
                        }
                        IconButton({}) {
                            Icon(
                                painter = painterResource(Res.drawable.edit),
                                contentDescription = "Edit stream",
                                tint = Color(0xFF44069A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .wrapContentHeight()
                            .width(328.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .width(220.dp)
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Конец:",
                                color = Color(0xFF8338EB),
                                fontFamily = mulishFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                letterSpacing = 0.sp
                            )
                            DateInputField(value = endDate, onValueChange = { endDate = it })
                        }
                        IconButton({}) {
                            Icon(
                                painter = painterResource(Res.drawable.edit),
                                contentDescription = "Edit stream",
                                tint = Color(0xFF44069A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(9.dp))

                    AddStreamMarket(
                        viewModel = object : StreamMarketState {
                            override var availableMarkets: List<NtiMarket> = emptyList()
                            override var selectedMarketIds: Set<String> = emptySet()
                            override fun toggleMarket(id: String) {}
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    StreamButton("Создать поток", {})
                }
            }
        }
    }
}