package com.example.track_me_mobile.features.team_card.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.core.ui.theme.*
import com.example.track_me_mobile.features.team_card.domain.models.TrackerUser
import com.example.track_me_mobile.features.teams.domain.models.NtiMarket
import com.example.track_me_mobile.features.teams.domain.models.Stream
import com.example.track_me_mobile.features.teams.presentation.DarkPurple
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.arrowback
import org.jetbrains.compose.resources.painterResource

@Composable
fun TeamEditScreen(
    viewModel: TeamEditViewModel,
    onSaved: () -> Unit,
    onDeactivated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow
    var showDeactivateDialog by remember { mutableStateOf(false) }

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
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(state.loadError!!, color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::retry,
                            colors = ButtonDefaults.buttonColors(containerColor = TrackMePurple)) {
                            Text("Повторить", color = Color.White)
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
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

                    Spacer(Modifier.height(20.dp))

                    if (state.isAdminRole) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DropdownObjectRow(
                                label           = "Трекер:",
                                options         = state.availableTrackers,
                                selectedValue   = state.selectedTracker,
                                displayName     = { it.fullName },
                                onValueSelected = viewModel::onTrackerSelected,
                                error           = state.trackerError
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    } else {
                        ReadonlyRow(label = "Трекер:", value = state.originalTeam?.username ?: "")
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Название команды:", fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value         = state.name,
                            onValueChange = viewModel::onNameChange,
                            singleLine    = true,
                            isError       = state.nameError != null,
                            placeholder   = { Text("Введите название", color = TextGray, fontSize = 14.sp) },
                            modifier      = Modifier.weight(1f),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = TrackMePurple,
                                unfocusedBorderColor = TrackMePurple.copy(alpha = 0.4f),
                                errorBorderColor     = Color.Red
                            ),
                            textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        )
                    }
                    if (state.nameError != null) {
                        Text(state.nameError!!, color = Color.Red, fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Ссылка на комнату:", fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value         = state.meetingRoomLink,
                            onValueChange = viewModel::onMeetingRoomLinkChange,
                            singleLine    = true,
                            isError       = state.meetingRoomLinkError != null,
                            placeholder   = { Text("Введите ссылку (пример: https://webinar.tusur.ru/b/...)", color = TextGray, fontSize = 14.sp) },
                            modifier      = Modifier.weight(1f),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = TrackMePurple,
                                unfocusedBorderColor = TrackMePurple.copy(alpha = 0.4f),
                                errorBorderColor     = Color.Red
                            ),
                            textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                        )
                    }
                    if (state.meetingRoomLinkError != null) {
                        Text(state.meetingRoomLinkError!!, color = Color.Red, fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    if (state.isAdminRole) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DropdownObjectRow(
                                label           = "Поток:",
                                options         = state.availableStreams,
                                selectedValue   = state.selectedStream,
                                displayName     = { it.name },
                                onValueSelected = viewModel::onStreamSelected,
                                error           = state.streamError
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    } else {
                        ReadonlyRow(label = "Поток:", value = state.originalTeam?.stream?.name ?: "")
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MultiDropdownObjectRow(
                            label           = "Рынки НТИ:",
                            options         = state.availableMarkets,
                            selectedValues  = state.selectedMarkets,
                            displayName     = { it.displayName },
                            onValuesChanged = viewModel::onMarketsChanged,
                            error           = state.marketsError
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DropdownSectionRow(
                            label           = "TRL:",
                            options         = TRL_OPTIONS,
                            selectedValue   = state.selectedTrl,
                            onValueSelected = viewModel::onTrlSelected,
                            error           = state.trlError
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text("Описание:", fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value         = state.description,
                        onValueChange = viewModel::onDescriptionChange,
                        isError       = state.descriptionError != null,
                        placeholder   = { Text("Описание карточки команды...", color = TextGray, fontSize = 14.sp) },
                        modifier      = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = TrackMePurple,
                            unfocusedBorderColor = TrackMePurple.copy(alpha = 0.4f),
                            errorBorderColor     = Color.Red
                        ),
                        textStyle    = TextStyle(fontSize = 14.sp, color = Color.Black),
                        maxLines     = 10,
                    )
                    if (state.descriptionError != null) {
                        Text(state.descriptionError!!, color = Color.Red, fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                    }

                    if (state.submitError != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(state.submitError!!, color = Color.Red, fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth()
                                .background(Color.Red.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(12.dp))
                    }

                    Spacer(Modifier.height(32.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick  = { viewModel.save(onSuccess = onSaved) },
                            enabled  = !state.isSubmitting,
                            modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = TrackMePurple),
                            shape    = RoundedCornerShape(50)
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Сохранить", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick  = { showDeactivateDialog = true },
                            modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3524E)),
                            shape    = RoundedCornerShape(50)
                        ) {
                            Text("Деактивировать", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            shape            = RoundedCornerShape(28.dp),
            containerColor   = Color.White,
            title = {
                Text(
                    "Вы уверены, что хотите остановить работу данной команды?",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = Color.Black, textAlign = TextAlign.Center
                )
            },
            text = if (state.deleteError != null) {
                { Text(state.deleteError!!, color = Color.Red, fontSize = 12.sp, textAlign = TextAlign.Center) }
            } else null,
            dismissButton = {
                Button(
                    onClick  = { showDeactivateDialog = false },
                    modifier = Modifier.height(50.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3524E)),
                    shape    = RoundedCornerShape(50)
                ) { Text("Нет", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeactivateDialog = false
                        viewModel.deactivate(onSuccess = onDeactivated)
                    },
                    modifier = Modifier.height(50.dp),
                    enabled  = !state.isDeleting,
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                    shape    = RoundedCornerShape(50)
                ) {
                    if (state.isDeleting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Да", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

@Composable
private fun ReadonlyRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = Color.Black)
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .background(TrackMePurple.copy(alpha = 0.12f), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(value, color = TrackMePurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}