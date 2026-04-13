package com.example.track_me_mobile.features.team_card.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.theme.*
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.features.teams.presentation.DarkPurple
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.arrowback
import org.jetbrains.compose.resources.painterResource

@Composable
fun TeamCreateScreen(
    viewModel: TeamCreateViewModel,
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { MainTopHeader() },
        containerColor = BackgroundWhite
    ) { padding ->

        when {
            state.isLoading -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TrackMePurple)
                }
            }

            state.loadError != null -> {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            state.loadError!!,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.retry() },
                            colors  = ButtonDefaults.buttonColors(containerColor = TrackMePurple)
                        ) {
                            Text("Повторить", color = Color.White)
                        }
                    }
                }
            }

            else -> {
                TeamCreateForm(
                    state       = state,
                    viewModel   = viewModel,
                    padding     = padding,
                    onBackClick = onBackClick,
                    onCreated   = onCreated
                )
            }
        }
    }
}

@Composable
private fun TeamCreateForm(
    state: CreateTeamUiState,
    viewModel: TeamCreateViewModel,
    padding: PaddingValues,
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {

    val navigator = LocalNavigator.currentOrThrow

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.arrowback),
            contentDescription = null,
            tint = DarkPurple,
            modifier = Modifier
                .size(24.dp)
                .clickable { navigator.pop() }
        )

        Spacer(Modifier.height(24.dp))
        CreateFormField(label = "Название команды:", error = state.teamNameError) {
            OutlinedTextField(
                value         = state.teamName,
                onValueChange = viewModel::onTeamNameChange,
                singleLine    = true,
                isError       = state.teamNameError != null,
                placeholder   = { Text("Введите название", color = TextGray, fontSize = 14.sp) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TrackMePurple,
                    unfocusedBorderColor = TrackMePurple.copy(alpha = 0.4f),
                    errorBorderColor     = Color.Red
                ),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black)
            )
        }

        Spacer(Modifier.height(20.dp))
        CreateFormField(label = "Ссылка на комнату:", error = state.meetingRoomLinkError) {
            OutlinedTextField(
                value         = state.meetingRoomLink,
                onValueChange = viewModel::onMeetingRoomChange,
                singleLine    = true,
                isError       = state.teamNameError != null,
                placeholder   = { Text("Введите ссылку (пример: https://webinar.tusur.ru/b/...)", color = TextGray, fontSize = 14.sp) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TrackMePurple,
                    unfocusedBorderColor = TrackMePurple.copy(alpha = 0.4f),
                    errorBorderColor     = Color.Red
                ),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black)
            )
        }

        Spacer(Modifier.height(20.dp))
        if (state.isTrackerRole) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Трекер:", fontSize = 14.sp, color = Color.Black)
                Spacer(Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .background(TrackMePurpleLight.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text       = state.selectedTracker?.fullName ?: "—",
                        color      = TrackMePurple,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            DropdownObjectRow(
                label           = "Трекер:",
                options         = state.trackers,
                selectedValue   = state.selectedTracker,
                displayName     = { it.fullName },
                onValueSelected = viewModel::onTrackerSelected,
                error           = state.trackerError
            )
        }
        Spacer(Modifier.height(16.dp))
        DropdownObjectRow(
            label           = "Поток:",
            options         = state.streams,
            selectedValue   = state.selectedStream,
            displayName     = { it.name },
            onValueSelected = viewModel::onStreamSelected,
            error           = state.streamError
        )
        Spacer(Modifier.height(16.dp))
        MultiDropdownObjectRow(
            label           = "Рынки НТИ:",
            options         = state.ntiMarkets,
            selectedValues  = state.selectedMarkets,
            displayName     = { it.displayName },
            onValuesChanged = viewModel::onMarketsChanged,
            error           = state.marketsError
        )
        Spacer(Modifier.height(16.dp))
        DropdownSectionRow(
            label           = "TRL:",
            options         = TRL_OPTIONS,
            selectedValue   = state.selectedTrl,
            onValueSelected = viewModel::onTrlSelected,
            error           = state.trlError
        )
        Spacer(Modifier.height(20.dp))
        CreateFormField(label = "Описание:", error = state.descriptionError) {
            OutlinedTextField(
                value         = state.description,
                onValueChange = viewModel::onDescriptionChange,
                isError       = state.descriptionError != null,
                placeholder   = { Text("Описание карточки команды...", color = TextGray, fontSize = 14.sp) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TrackMePurple,
                    unfocusedBorderColor = TrackMePurple.copy(alpha = 0.4f),
                    errorBorderColor     = Color.Red
                ),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                maxLines  = 10,
            )
        }
        if (state.submitError != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text     = state.submitError,
                color    = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick  = { viewModel.submit(onSuccess = onCreated) },
                enabled  = !state.isSubmitting,
                colors   = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                modifier = Modifier.width(200.dp).height(48.dp),
                shape    = RoundedCornerShape(50)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Создать", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CreateFormField(
    label: String,
    error: String?,
    content: @Composable () -> Unit
) {
    Column {
        Text(text = label, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 6.dp))
        content()
        if (error != null) {
            Text(text = error, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}

@Composable
fun <T> DropdownObjectRow(
    label: String,
    options: List<T>,
    selectedValue: T?,
    displayName: (T) -> String,
    onValueSelected: (T) -> Unit,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, fontSize = 14.sp, color = Color.Black)
            Spacer(Modifier.width(16.dp))
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (error != null) Color.Red else TrackMePurple)
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text       = if (selectedValue == null) "+" else displayName(selectedValue),
                        color      = Color.White,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        val isSelected = option == selectedValue
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text       = displayName(option),
                                    color      = if (isSelected) TrackMePurple else Color.Black,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = { onValueSelected(option); expanded = false }
                        )
                    }
                }
            }
        }
        if (error != null) {
            Text(error, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}

@Composable
fun <T> MultiDropdownObjectRow(
    label: String,
    options: List<T>,
    selectedValues: List<T>,
    displayName: (T) -> String,
    onValuesChanged: (List<T>) -> Unit,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = remember(selectedValues) { selectedValues.toMutableStateList() }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, fontSize = 14.sp, color = Color.Black)
            Spacer(Modifier.width(16.dp))
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (error != null) Color.Red else TrackMePurple)
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text       = if (selected.isEmpty()) "+" else "${selected.size} выбрано",
                        color      = Color.White,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                DropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = { expanded = false; onValuesChanged(selected.toList()) }
                ) {
                    options.forEach { option ->
                        val isChecked = selected.any { displayName(it) == displayName(option) }
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked         = isChecked,
                                        onCheckedChange = null,
                                        colors          = CheckboxDefaults.colors(
                                            checkedColor   = TrackMePurple,
                                            uncheckedColor = Color.Gray
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(displayName(option), color = Color.Black)
                                }
                            },
                            onClick = {
                                if (isChecked) selected.removeAll { displayName(it) == displayName(option) }
                                else selected.add(option)
                                onValuesChanged(selected.toList())
                            }
                        )
                    }
                }
            }
        }
        if (error != null) {
            Text(error, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}

@Composable
fun DropdownSectionRow(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, fontSize = 14.sp, color = Color.Black)
            Spacer(Modifier.width(16.dp))
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (error != null) Color.Red else TrackMePurple)
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text       = if (selectedValue.isEmpty()) "+" else selectedValue,
                        color      = Color.White,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text       = option,
                                    color      = if (option == selectedValue) TrackMePurple else Color.Black,
                                    fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = { onValueSelected(option); expanded = false }
                        )
                    }
                }
            }
        }
        if (error != null) {
            Text(error, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}