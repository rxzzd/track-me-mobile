package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.track_me_mobile.core.ui.theme.*
import androidx.compose.foundation.text.BasicTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamEditScreen(
    viewModel: TeamViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeactivateClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    val currentData by viewModel.teamData.collectAsState()

    var localData by remember(currentData) { mutableStateOf(currentData) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val streams = listOf("Название потока 1", "Название потока 2", "Название потока 3")
    val markets = listOf("AutoNet", "HealthNet", "MariNet", "NeuroNet", "SafeNet", "FoodNet", "TechNet", "WearNet")
    val trlList = listOf("0-2", "3-5", "6-8", "9-10")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TrackMe", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onFilterClick) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "←",
                color = TrackMePurple,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrackerRow(
                name = localData.trackerName,
                onNameChanged = { localData = localData.copy(trackerName = it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DropdownSectionRow(
                label = "Поток:",
                options = streams,
                selectedValue = localData.stream,
                onValueSelected = { localData = localData.copy(stream = it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MultiDropdownSectionRow(
                label = "Рынки НТИ:",
                options = markets,
                selectedValues = localData.markets,
                onValuesChanged = { localData = localData.copy(markets = it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                DropdownSectionRow(
                    label = "TRL:",
                    options = trlList,
                    selectedValue = localData.trl,
                    onValueSelected = { localData = localData.copy(trl = it) }
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onFilterClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Фильтр", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val descriptionScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.5.dp, TrackMePurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))  // рамка всегда чёткая
            ) {
                BasicTextField(
                    value = localData.description,
                    onValueChange = { localData = localData.copy(description = it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 20.dp)
                        .verticalScroll(descriptionScrollState),
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                    maxLines = Int.MAX_VALUE,
                    decorationBox = { innerTextField ->
                        if (localData.description.isEmpty()) {
                            Text("Описание карточки команды...", color = Color.Gray, fontSize = 14.sp)
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        viewModel.applyEdit(localData)
                        onSaveClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Сохранить", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3524E)),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Деактивировать", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            content = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(4.dp, Color(0xFFD3524E), RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            IconButton(onClick = { showDeleteDialog = false }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFD3524E)
                                )
                            }
                        }

                        Text(
                            text = "Вы уверены, что хотите остановить работу данной команды?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { showDeleteDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3524E)),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text("Нет", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    showDeleteDialog = false
                                    viewModel.reset()
                                    onDeactivateClick()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text("Да", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        )
    }
}