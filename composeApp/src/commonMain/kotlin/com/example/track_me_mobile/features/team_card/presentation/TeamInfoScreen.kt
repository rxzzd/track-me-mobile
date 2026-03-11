package com.example.track_me_mobile.features.team_card.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.core.ui.theme.*
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
@Composable
fun TeamInfoScreen(
    viewModel: TeamCardViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onMeetingsClick: () -> Unit
) {
    when (val s = viewModel.state) {
        is TeamCardState.Loading ->
            TeamInfoLoadingScreen()
        is TeamCardState.Error ->
            TeamInfoErrorScreen(s.message, viewModel::loadTeam)
        is TeamCardState.Success ->
            TeamInfoContentScreen(
                team            = s.team,
                trackerFullName = s.trackerFullName,
                onBackClick     = onBackClick,
                onEditClick     = onEditClick,
                onMeetingsClick = onMeetingsClick
            )
    }
}

@Composable
private fun TeamInfoLoadingScreen() {
    Scaffold(
        topBar = { MainTopHeader() },
        containerColor = BackgroundWhite
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = TrackMePurple)
        }
    }
}

@Composable
private fun TeamInfoErrorScreen(message: String, onRetry: () -> Unit) {
    Scaffold(
        topBar = { MainTopHeader() },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(message, color = ErrorRed, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple)
            ) { Text("Повторить") }
        }
    }
}

@Composable
private fun TeamInfoContentScreen(
    team: TeamCard,
    trackerFullName: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onMeetingsClick: () -> Unit
) {
    val descScrollState = rememberScrollState()

    Scaffold(
        topBar = { MainTopHeader() },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // ── Назад ──────────────────────────────────────────────
            Text(
                text = "←",
                color = TrackMePurple,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackClick() }
            )

            Spacer(Modifier.height(16.dp))

            // ── Информация о потоке (как на макете) ─────────────────
            InfoTextRow(
                label    = "Название потока:",
                value    = team.stream?.name ?: "—",
                isPurple = true
            )
            val teamsCount = team.teamsCount ?: 0
            InfoTextRow(
                label    = "Кол-во команд:",
                value    = "$teamsCount",
                isPurple = true
            )
            val dateRange = team.stream?.let {
                val start = it.startDate ?: "—"
                val end   = it.endDate ?: "—"
                "$start - $end"
            } ?: "—"
            InfoTextRow(
                label    = "Дата проведения:",
                value    = dateRange,
                isPurple = true
            )

            // ── Рейтинг ─────────────────────────────────────────────
            team.averageGrade?.let {
                InfoTextRow(
                    label    = "Средняя оценка:",
                    value = (kotlin.math.round(it * 100) / 100.0).toString(),
                    isPurple = true
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Трекер ──────────────────────────────────────────────
            TrackerRow(name = trackerFullName)

            Spacer(Modifier.height(16.dp))

            // ── Рынки НТИ ───────────────────────────────────────────
            Text("Рынки НТИ:", fontSize = 14.sp, color = TextBlack)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 3, // Теперь это будет работать правильно
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (team.ntiMarkets.isEmpty()) {
                    PurpleChip("—")
                } else {
                    team.ntiMarkets.forEach { market ->
                        // Оставляем ТОЛЬКО чип.
                        // Если PurpleChip принимает Modifier, добавьте .weight(1f)
                        PurpleChip(market.displayName)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── TRL ─────────────────────────────────────────────────
            Text("TRL:", fontSize = 14.sp, color = TextBlack)
            Spacer(Modifier.height(8.dp))
            PurpleChip(team.readinessLevel.ifEmpty { "—" })

            Spacer(Modifier.height(24.dp))

            // ── Описание ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.5.dp, TrackMePurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = team.description.ifEmpty { "Описание отсутствует" },
                    color = if (team.description.isEmpty()) Color.Gray else TextBlack,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 20.dp)
                        .verticalScroll(descScrollState)
                )
                // Кастомный скроллбар
                val maxScroll = descScrollState.maxValue
                val curScroll = descScrollState.value
                if (maxScroll > 0) {
                    val thumbH   = 150f / (150f + maxScroll)
                    val thumbTop = curScroll.toFloat() / maxScroll * (1f - thumbH)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 6.dp, top = 12.dp, bottom = 12.dp)
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(TrackMePurple, RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .width(4.dp)
                                .fillMaxHeight(thumbH)
                                .offset(y = (126.dp * thumbTop))
                                .background(Color.White, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Статистика встреч:", fontSize = 14.sp, color = TextBlack)
            Spacer(Modifier.height(8.dp))
            // ── Статистика встреч ───────────────────────────────────
            if (team.meetingsCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TrackMePurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MeetingStatItem("Всего",        team.meetingsCount)
                    MeetingStatItem("Проведено",    team.meetingsCompletedCount)
                    MeetingStatItem("Не состоялось",team.meetingsNotHappenedCount)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Кнопки ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onEditClick,
                    modifier = Modifier.height(45.dp)
                ) {
                    Text(
                        "Редактировать",
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



@Composable
private fun MeetingStatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TrackMePurple
        )
        Text(text = label, fontSize = 11.sp, color = TextBlack)
    }
}