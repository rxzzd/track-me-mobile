package com.example.track_me_mobile.features.reports.presentation

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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.features.reports.presentation.components.*
import com.example.track_me_mobile.features.reports.domain.models.ReportItem
import com.example.track_me_mobile.features.team_card.presentation.InfoTeamLevel

class ReportsListScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<ReportsViewModel>()

        val navigator = LocalNavigator.currentOrThrow
        ReportsListContent(
            state = viewModel.state,
            selectedTracker = viewModel.selectedTracker,
            selectedStream = viewModel.selectedStream,
            availableTrackers = viewModel.availableTrackers,
            availableStreams = viewModel.availableStreams,
            showInactive = viewModel.showInactive,
            onTrackerSelected = viewModel::setTrackerFilter,
            onStreamSelected = viewModel::setStreamFilter,
            onToggleInactive = viewModel::toggleShowInactive,
            onDownloadReport = viewModel::downloadReport,
            onRetry = viewModel::loadReports,
            onStreamClick = { streamId ->
                if (!streamId.isNullOrBlank()) {
                    navigator.push(StreamMeetingReportScreen(streamId))
                }
            },
            onTeamClick = { teamId ->
                if (!teamId.isNullOrBlank()) {
                    navigator.push(InfoTeamLevel(teamId))
                }
            }
        )
    }
}

@Composable
fun ReportsListContent(
    state: ReportsState,
    selectedTracker: String,
    selectedStream: String,
    availableTrackers: List<ReportsViewModel.TrackerInfo>,
    availableStreams: List<String>,
    showInactive: Boolean,
    onTrackerSelected: (String) -> Unit,
    onStreamSelected: (String) -> Unit,
    onToggleInactive: () -> Unit,
    onDownloadReport: () -> Unit,
    onRetry: () -> Unit,
    onStreamClick: (String?) -> Unit,
    onTeamClick: (String?) -> Unit
) {
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

            // Кнопка выгрузки
            OutlinedButton(
                onClick = onDownloadReport,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                border = BorderStroke(2.dp, TrackMePurple),
                shape = RoundedCornerShape(20.dp)  // Менее круглый
            ) {
                Text(
                    "Выгрузить отчет",
                    color = TrackMePurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Переключатель неактивные
            OutlinedButton(
                onClick = onToggleInactive,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                border = BorderStroke(2.dp, TrackMePurple),
                shape = RoundedCornerShape(20.dp)  // Менее круглый
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Показывать неактивные",
                        color = TrackMePurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Switch(
                        checked = showInactive,
                        onCheckedChange = { onToggleInactive() },
                        modifier = Modifier.height(20.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TrackMePurple,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dropdown Трекеры
            val trackerDisplayName = if (selectedTracker == "Все") {
                "Все"
            } else {
                availableTrackers.find { it.username == selectedTracker }?.fullName ?: "Все"
            }

            ExpandableDropdown(
                title = "Трекеры",
                selectedItem = trackerDisplayName,
                items = listOf("Все") + availableTrackers.map { it.fullName },
                onItemSelected = { fullName ->
                    if (fullName == "Все") {
                        onTrackerSelected("Все")
                    } else {
                        val tracker = availableTrackers.find { it.fullName == fullName }
                        onTrackerSelected(tracker?.username ?: "Все")
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Dropdown Потоки
            ExpandableDropdown(
                title = "Потоки",
                selectedItem = selectedStream,
                items = if (availableStreams.isEmpty()) listOf("Все") else availableStreams,
                onItemSelected = onStreamSelected
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(thickness = 2.dp, color = TrackMePurple)

            Spacer(modifier = Modifier.height(12.dp))

            // Таблица - КАК БЫЛО
            when (state) {
                is ReportsState.Loading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TrackMePurple)
                    }
                }

                is ReportsState.Error -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = Color.Gray, fontSize = 14.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple)
                            ) {
                                Text("Повторить")
                            }
                        }
                    }
                }

                is ReportsState.Success -> {
                    if (state.reports.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(2.dp, TrackMePurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Нет данных", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                                Text("Измените фильтры", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        // Таблица - ВЕРНУЛИ КАК БЫЛО
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(2.dp, TrackMePurple)
                        ) {
                            Row(modifier = Modifier.horizontalScroll(horizState)) {
                                Column(modifier = Modifier.verticalScroll(vertState)) {
                                    // Заголовок
                                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                        TableCell("№", 50.dp, isHeader = true)
                                        TableCell("Название\nпотока", 180.dp, isHeader = true)
                                        TableCell("Сроки\nпотока", 160.dp, isHeader = true)
                                        TableCell("Название\nкоманды", 200.dp, isHeader = true)
                                        TableCell("Имя трекера", 220.dp, isHeader = true)
                                        TableCell("Средняя\nоценка\nкоманды", 120.dp, isHeader = true)
                                        TableCell("Средняя\nоценка\nтрекера", 120.dp, isHeader = true)
                                        TableCell("Трекшен-митинг\n(факт/план)", 160.dp, isHeader = true)
                                        TableCell("Рынки НТИ", 120.dp, isHeader = true)
                                        TableCell("Уровень TRL", 110.dp, isHeader = true)
                                    }

                                    // Строки
                                    state.reports.forEachIndexed { index, report ->
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
                                                modifier = Modifier.clickable(
                                                    enabled = !report.streamId.isNullOrBlank()
                                                ) {
                                                    report.streamId?.let(onStreamClick)
                                                }
                                            ) {
                                                TableCell(
                                                    text = report.streamName,
                                                    width = 180.dp,
                                                    textColor = if (!report.streamId.isNullOrBlank()) TrackMePurple else Color.Black
                                                )
                                            }
                                            TableCell("${report.startDate} - ${report.endDate}", 160.dp)
                                            Box(
                                                modifier = Modifier.clickable(
                                                    enabled = !report.teamId.isNullOrBlank()
                                                ) {
                                                    report.teamId?.let(onTeamClick)
                                                }
                                            ) {
                                                TableCell(
                                                    text = report.teamCardName,
                                                    width = 200.dp,
                                                    textColor = if (!report.teamId.isNullOrBlank()) TrackMePurple else Color.Black
                                                )
                                            }

                                            // ФИО трекера
                                            val trackerName = availableTrackers
                                                .find { it.username == report.username }
                                                ?.fullName ?: report.username
                                            TableCell(trackerName, 220.dp)

                                            TableCell(
                                                (kotlin.math.round(report.averageTeamGrade * 100) / 100).toString(),
                                                120.dp
                                            )
                                            TableCell(
                                                (kotlin.math.round(report.averageUserGrade * 100) / 100).toString(),
                                                120.dp
                                            )
                                            TableCell("${report.meetingsCountFact}/${report.meetingsCountPlan}", 160.dp)
                                            TableCell(report.ntiMarkets.joinToString(", "), 120.dp)
                                            TableCell(report.readinessLevel, 110.dp)
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ExpandableDropdown(
    title: String,
    selectedItem: String,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            border = BorderStroke(2.dp, TrackMePurple),
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,  // Менее круглый
                bottomStart = if (expanded) 0.dp else 20.dp,
                bottomEnd = if (expanded) 0.dp else 20.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedItem == "Все") title else selectedItem,
                    color = TrackMePurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(text = if (expanded) "▲" else "▼", color = TrackMePurple, fontSize = 12.sp)
            }
        }

        if (expanded) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),  // Менее круглый
                border = BorderStroke(2.dp, TrackMePurple),
                color = Color.White
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    items.forEach { item ->
                        Text(
                            text = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemSelected(item)
                                    expanded = false
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            color = if (item == selectedItem) TrackMePurple else Color.Black,
                            fontWeight = if (item == selectedItem) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                        if (item != items.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }
            }
        }
    }
}