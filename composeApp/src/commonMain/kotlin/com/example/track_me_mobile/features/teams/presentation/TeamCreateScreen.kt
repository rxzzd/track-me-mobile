package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.core.ui.theme.*
import androidx.compose.foundation.text.BasicTextField
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCreateScreen(
    viewModel: TeamViewModel,
    onBackClick: () -> Unit,
    onNavigateToInfo: () -> Unit
) {
    val teamData by viewModel.teamData.collectAsState()

    val streams = listOf("Название потока 1", "Название потока 2", "Название потока 3")
    val markets = listOf("AutoNet", "HealthNet", "MariNet", "NeuroNet", "SafeNet", "FoodNet", "TechNet", "WearNet")
    val trlList = listOf("0-2", "3-5", "6-8", "9-10")

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
                .imePadding()
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

            Spacer(modifier = Modifier.height(24.dp))

            TrackerRow(
                name = teamData.trackerName,
                onNameChanged = { viewModel.updateTrackerName(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            DropdownSectionRow(
                label = "Поток:",
                options = streams,
                selectedValue = teamData.stream,
                onValueSelected = { viewModel.updateStream(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MultiDropdownSectionRow(
                label = "Рынки НТИ:",
                options = markets,
                selectedValues = teamData.markets,
                onValuesChanged = { viewModel.updateMarkets(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DropdownSectionRow(
                label = "TRL:",
                options = trlList,
                selectedValue = teamData.trl,
                onValueSelected = { viewModel.updateTrl(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            val descriptionScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.5.dp, TrackMePurple, RoundedCornerShape(16.dp))
            ) {
                BasicTextField(
                    value = teamData.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 20.dp)
                        .verticalScroll(descriptionScrollState),
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                    maxLines = Int.MAX_VALUE,
                    decorationBox = { innerTextField ->
                        if (teamData.description.isEmpty()) {
                            Text("Описание карточки команды...", color = TextGray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
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

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { onNavigateToInfo() },
                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                    modifier = Modifier.width(200.dp).height(48.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Создать", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DropdownSectionRow(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = Color.Black)
        Spacer(modifier = Modifier.width(16.dp))

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(TrackMePurple)
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (selectedValue.isEmpty()) "+" else selectedValue,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = if (option == selectedValue) TrackMePurple else Color.Black,
                                fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MultiDropdownSectionRow(
    label: String,
    options: List<String>,
    selectedValues: List<String>,
    onValuesChanged: (List<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = remember(selectedValues) { selectedValues.toMutableStateList() }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = Color.Black)
        Spacer(modifier = Modifier.width(16.dp))

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(TrackMePurple)
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (selected.isEmpty()) "+" else "${selected.size} выбрано",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    onValuesChanged(selected.toList())
                }
            ) {
                options.forEach { option ->
                    val isChecked = selected.contains(option)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = TrackMePurple,
                                        uncheckedColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = option, color = Color.Black)
                            }
                        },
                        onClick = {
                            if (isChecked) selected.remove(option) else selected.add(option)
                            onValuesChanged(selected.toList())
                        }
                    )
                }
            }
        }
    }
}