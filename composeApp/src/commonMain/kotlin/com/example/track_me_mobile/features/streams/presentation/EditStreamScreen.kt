package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.features.streams.presentation.components.*
import org.jetbrains.compose.resources.Font
import com.example.track_me_mobile.generated.resources.Mulish_SemiBold
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.first_pencil
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.generated.resources.montserrat_bold
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.painterResource

class EditStreamScreen(
    private val streamId: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = koinInject<StreamRepository>()
        val viewModel = remember { EditStreamViewModel(repository, streamId) }

        EditStreamPageContent(
            viewModel = viewModel,
            onBack = { navigator.pop() },
            onTeamClick = { teamId ->
                // Навигация на экран команды через InfoTeamLevel
                navigator.push(
                    com.example.track_me_mobile.features.team_card.presentation.InfoTeamLevel(teamId)
                )
            }
        )
    }
}

@Composable
fun EditStreamPageContent(
    viewModel: EditStreamViewModel,
    onBack: () -> Unit,
    onTeamClick: (String) -> Unit
) {
    val mulishFamily = FontFamily(
        Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold)
    )

    val montserratFamily = FontFamily(Font(Res.font.montserrat_bold, FontWeight.SemiBold))

    val rowWidth = 328.dp
    val labelWidth = 135.dp
    val globalIconSize = 20.dp

    var showDeleteDialog by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            topBar = { MainTopHeader(onBackClick = onBack) },
            containerColor = Color(0xFFF8F3FF)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color(0xFFF8F3FF))
                    .verticalScroll(rememberScrollState())
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Редактирование потока",
                    fontSize = 32.sp,
                    color = Color(0xFF44069A),
                    fontFamily = mulishFamily,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
                AddStreamPhoto()
                Spacer(modifier = Modifier.height(20.dp))

                // Поле НАЗВАНИЕ
                Row(
                    modifier = Modifier.width(rowWidth),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(20.dp))

                    StreamNameInput(
                        value = viewModel.name,
                        onValueChange = { viewModel.name = it }
                    )
                    EditPencilIcon(isActive = viewModel.name.isNotBlank(), size = globalIconSize)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier.width(rowWidth),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Поле НАЧАЛО
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Начало:",
                            modifier = Modifier.width(labelWidth),
                            color = Color(0xFF8338EB),
                            fontFamily = mulishFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        DateInputField(
                            value = viewModel.startDate,
                            onValueChange = { viewModel.startDate = it }
                        )
                        EditPencilIcon(isActive = viewModel.startDate.isNotBlank(), size = globalIconSize)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Поле КОНЕЦ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Конец:",
                            modifier = Modifier.width(labelWidth),
                            color = Color(0xFF8338EB),
                            fontFamily = mulishFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        DateInputField(
                            value = viewModel.endDate,
                            onValueChange = { viewModel.endDate = it }
                        )
                        EditPencilIcon(isActive = viewModel.endDate.isNotBlank(), size = globalIconSize)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Поле ТРЕКШЕН-МИТИНГ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.width(labelWidth)) {
                            Text(
                                text = "Дата начала",
                                color = Color(0xFF8338EB),
                                fontFamily = mulishFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                lineHeight = 14.sp
                            )
                            Text(
                                text = "трекшен-митинга:",
                                color = Color(0xFF8338EB),
                                fontFamily = mulishFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                lineHeight = 14.sp
                            )
                        }
                        DateInputField(
                            value = viewModel.trackStartDate,
                            onValueChange = { viewModel.trackStartDate = it }
                        )
                        EditPencilIcon(isActive = viewModel.trackStartDate.isNotBlank(), size = globalIconSize)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Количество встреч:",
                        color = Color(0xFF8338EB),
                        fontFamily = mulishFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.size(globalIconSize))

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            MeetingsCountDropdown(
                                value = viewModel.meetingsCount,
                                onValueChange = { viewModel.meetingsCount = it }
                            )
                        }

                        EditPencilIcon(isActive = viewModel.meetingsCount > 0, size = globalIconSize)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                AddStreamMarket(viewModel = viewModel)
                Spacer(modifier = Modifier.height(14.dp))

                viewModel.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        fontFamily = mulishFamily,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                StreamButton(
                    text = if (viewModel.isLoading) "Сохранение..." else "Сохранить",
                    onClick = { viewModel.updateStream(onSuccess = onBack) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка удаления
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .width(140.dp)
                        .height(35.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE57373)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Удалить",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = montserratFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Диалог подтверждения удаления
        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    showDeleteDialog = false
                    viewModel.deleteStream(onSuccess = onBack)
                },
                mulishFamily = mulishFamily
            )
        }

        // НОВЫЙ ДИАЛОГ: Ошибка с командами
        if (viewModel.showTeamsConflictDialog) {
            StreamWithTeamsErrorDialog(
                teams = viewModel.teamsInStream,
                onDismiss = { viewModel.dismissTeamsDialog() },
                onTeamClick = { teamId ->
                    viewModel.dismissTeamsDialog()
                    onTeamClick(teamId)
                }
            )
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    mulishFamily: FontFamily
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .border(2.dp, Color(0xFFE57373), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .size(24.dp),
                        tint = Color(0xFFE57373)
                    )
                }

                Text(
                    text = "Вы уверены, что хотите\nудалить данный поток?",
                    textAlign = TextAlign.Center,
                    fontFamily = mulishFamily,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text("Нет", color = Color.White, fontFamily = mulishFamily)
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text("Да", color = Color.White, fontFamily = mulishFamily)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditPencilIcon(
    isActive: Boolean,
    size: Dp = 20.dp
) {
    val tint = if (isActive) Color(0xFF44069A) else Color(0xFFB8A5F0)

    Icon(
        painter = painterResource(Res.drawable.first_pencil),
        contentDescription = "Edit field",
        tint = tint,
        modifier = Modifier.size(size)
    )
}