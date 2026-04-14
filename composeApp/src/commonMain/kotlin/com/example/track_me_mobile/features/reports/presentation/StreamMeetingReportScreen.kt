package com.example.track_me_mobile.features.reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.features.reports.presentation.components.*
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import com.example.track_me_mobile.features.reports.domain.models.StreamMeetingReportItem
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
class StreamMeetingReportScreen(
    private val streamId: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = remember(streamId) {
            object : KoinComponent {}.getKoin().get<StreamMeetingReportViewModel> { parametersOf(streamId) }
        }

        val vertState = rememberScrollState()
        val horizState = rememberScrollState()

        Scaffold(
            topBar = { MainTopHeader() },
            containerColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Отчёт по встречам",
                        color = TrackMePurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Button(
                        onClick = { navigator.pop() },
                        colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple)
                    ) {
                        Text("Назад", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExpandableDropdown(
                    title = "Трекеры",
                    selectedItem = viewModel.selectedTracker,
                    items = viewModel.availableTrackers,
                    onItemSelected = viewModel::setTrackerFilter
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExpandableDropdown(
                    title = "Статусы",
                    selectedItem = viewModel.selectedStatus,
                    items = viewModel.availableStatuses,
                    onItemSelected = viewModel::setStatusFilter
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExpandableDropdown(
                    title = "Команды",
                    selectedItem = viewModel.selectedTeam,
                    items = viewModel.availableTeams,
                    onItemSelected = viewModel::setTeamFilter
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(thickness = 2.dp, color = TrackMePurple)
                Spacer(modifier = Modifier.height(12.dp))

                when (val state = viewModel.state) {
                    is StreamMeetingReportState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = TrackMePurple)
                        }
                    }
                    is StreamMeetingReportState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = Color.Gray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = viewModel::loadReports,
                                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple)
                                ) {
                                    Text("Повторить", color = Color.White)
                                }
                            }
                        }
                    }
                    is StreamMeetingReportState.Success -> {
                        val items = state.items
                        val title = items.firstOrNull()?.streamName?.takeIf { it.isNotBlank() } ?: "Отчёт по встречам"

                        Text(
                            text = title,
                            modifier = Modifier.padding(horizontal = 0.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (items.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .border(2.dp, TrackMePurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Нет данных",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text("Измените фильтры", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .border(2.dp, TrackMePurple)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Row(modifier = Modifier.horizontalScroll(horizState)) {
                                        Column(modifier = Modifier.verticalScroll(vertState)) {
                                            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                                TableCell("№", 50.dp, isHeader = true)
                                                TableCell("Команда", 180.dp, isHeader = true)
                                                TableCell("Дата", 110.dp, isHeader = true)
                                                TableCell("Трекер", 180.dp, isHeader = true)
                                                TableCell("Задачи к следующей встрече", 220.dp, isHeader = true)
                                                TableCell("Выполнение задач / инфо", 220.dp, isHeader = true)
                                                TableCell("Статус", 170.dp, isHeader = true)
                                            }

                                            items.forEachIndexed { index, item ->
                                                Row(
                                                    modifier = Modifier
                                                        .height(IntrinsicSize.Min)
                                                        .background(
                                                            if (index % 2 == 0) Color.White
                                                            else Color(0xFFF3F0FF)
                                                        )
                                                ) {
                                                    TableCell((index + 1).toString(), 50.dp)
                                                    Box(
                                                        modifier = Modifier
                                                            .clickable(enabled = !item.teamId.isNullOrBlank()) {
                                                                item.teamId?.let {
                                                                    navigator.push(com.example.track_me_mobile.features.team_card.presentation.InfoTeamLevel(it))
                                                                }
                                                            }
                                                    ) {
                                                        TableCell(
                                                            item.teamName,
                                                            180.dp,
                                                            textColor = if (!item.teamId.isNullOrBlank()) TrackMePurple else Color.Black
                                                        )
                                                    }
                                                    TableCell(item.startDate ?: "—", 110.dp)
                                                    TableCell(
                                                        item.trackerFullName ?: item.trackerName ?: "—",
                                                        180.dp
                                                    )
                                                    TableCell(
                                                        item.tasksNextMeeting.orEmpty().ifBlank { "—" },
                                                        220.dp
                                                    )
                                                    TableCell(
                                                        item.tasksCurrentMeeting.orEmpty().ifBlank { "—" },
                                                        220.dp
                                                    )
                                                    TableCell(
                                                        item.teamStatus ?: item.status ?: "—",
                                                        170.dp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    CustomVerticalScrollbar(
                                        vertState,
                                        Modifier.align(Alignment.CenterEnd).padding(top = 44.dp)
                                    )
                                    CustomHorizontalScrollbar(
                                        horizState,
                                        Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MeetingRow(
    index: Int,
    item: StreamMeetingReportItem,
    onTeamClick: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "${index + 1}", modifier = Modifier.weight(0.4f), fontSize = 13.sp)

        Text(
            text = item.teamName,
            modifier = Modifier
                .weight(2f)
                .clickable(enabled = item.teamId != null) { onTeamClick(item.teamId) },
            color = if (item.teamId != null) TrackMePurple else Color.Black,
            fontSize = 13.sp
        )

        Text(text = item.startDate ?: "—", modifier = Modifier.weight(1.2f), fontSize = 13.sp)

        Text(
            text = item.trackerFullName ?: item.trackerName ?: "—",
            modifier = Modifier.weight(1.8f),
            fontSize = 13.sp
        )

        Text(
            text = item.tasksNextMeeting.orEmpty().ifBlank { "—" },
            modifier = Modifier.weight(2.5f),
            fontSize = 13.sp
        )

        Text(
            text = item.tasksCurrentMeeting.orEmpty().ifBlank { "—" },
            modifier = Modifier.weight(2.5f),
            fontSize = 13.sp
        )

        Text(
            text = item.teamStatus ?: item.status ?: "—",
            modifier = Modifier.weight(1.7f),
            fontSize = 13.sp
        )
    }
}
