package com.example.track_me_mobile.features.team_card.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamInfoScreen(
    viewModel: TeamViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onMeetingsClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    val data by viewModel.teamData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TrackMe", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TrackMePurple)
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "←",
                color = TrackMePurple,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoTextRow(label = "Название потока:", value = data.stream.ifEmpty { "Не указано" }, isPurple = true)
            InfoTextRow(label = "Кол-во команд:", value = "12 команд", isPurple = true)
            InfoTextRow(label = "Дата проведения:", value = "01.01.2025 - 10.10.2025", isPurple = true)

            Spacer(modifier = Modifier.height(24.dp))

            TrackerRow(name = data.trackerName)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Рынки НТИ:", fontSize = 14.sp, color = TextBlack)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                if (data.markets.isEmpty()) {
                    PurpleChip(text = "Нет рынков")
                } else {
                    data.markets.forEach { market ->
                        PurpleChip(text = market)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("TRL:", fontSize = 14.sp, color = TextBlack)
            Spacer(modifier = Modifier.height(8.dp))
            PurpleChip(text = data.trl.ifEmpty { "Не указано" })

            Spacer(modifier = Modifier.height(24.dp))

            val descriptionScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.5.dp, TrackMePurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = data.description.ifEmpty { "Описание отсутствует" },
                    color = if (data.description.isEmpty()) Color.Gray else TextBlack,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 20.dp)
                        .verticalScroll(descriptionScrollState)
                )

                val maxScroll = descriptionScrollState.maxValue
                val currentScroll = descriptionScrollState.value

                if (maxScroll > 0) {
                    val thumbHeightFraction = 150f / (150f + maxScroll)
                    val thumbTopFraction = currentScroll.toFloat() / maxScroll * (1f - thumbHeightFraction)

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 6.dp)
                            .width(6.dp)
                            .fillMaxHeight()
                            .padding(vertical = 12.dp)
                            .background(TrackMePurple, RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .width(4.dp)
                                .fillMaxHeight(thumbHeightFraction)
                                .offset(y = (126.dp * thumbTopFraction))
                                .background(Color.White, RoundedCornerShape(2.dp))
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 6.dp)
                            .width(6.dp)
                            .fillMaxHeight()
                            .padding(vertical = 12.dp)
                            .border(1.dp, TrackMePurple.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                            .background(Color.White, RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onEditClick, modifier = Modifier.height(45.dp)) {
                    Text(
                        text = "Редактировать",
                        color = TrackMePurple,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onMeetingsClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(45.dp).width(150.dp)
                ) {
                    Text("Встречи", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
