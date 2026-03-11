package com.example.track_me_mobile.features.streams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.features.streams.presentation.components.*
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import com.example.track_me_mobile.generated.resources.Mulish_SemiBold
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.arrowback

class AddStreamScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<AddStreamViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        AddStreamPageContent(
            viewModel = viewModel,
            onSuccess = { navigator.pop() },
            onBack = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStreamPageContent(
    viewModel: AddStreamViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val mulishFamily = FontFamily(
        Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold)
    )

    val rowWidth = 300.dp
    val labelWidth = 150.dp

    MaterialTheme {
        Scaffold(
            topBar = { MainTopHeader() },
            containerColor = Color(0xFFF8F3FF)
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Скроллируемый контент
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .background(Color(0xFFF8F3FF))
                        .verticalScroll(rememberScrollState())
                        .padding(top = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Заголовок со стрелкой назад
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Текст по центру
                        Text(
                            text = "Создание потока",
                            fontSize = 32.sp,
                            color = Color(0xFF44069A),
                            fontFamily = mulishFamily,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp,
                        )

                        // Стрелка слева от текста
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(start = 8.dp),
//                            horizontalArrangement = Arrangement.Start,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            IconButton(onClick = onBack) {
//                                Icon(
//                                    painter = painterResource(Res.drawable.arrowback),
//                                    contentDescription = "Back",
//                                    tint = Color(0xFF8338EB),
//                                    modifier = Modifier.size(24.dp)
//                                )
//                            }
//                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AddStreamPhoto()

                    Spacer(modifier = Modifier.height(20.dp))

                    StreamNameInput(
                        value = viewModel.name,
                        onValueChange = { viewModel.name = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .width(rowWidth)
                            .padding(start = 20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // НАЧАЛО ПОТОКА
                        Row(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            verticalAlignment = Alignment.CenterVertically
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
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // КОНЕЦ ПОТОКА
                        Row(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            verticalAlignment = Alignment.CenterVertically
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
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // ТРЕКШЕН-МИТИНГ
                        Row(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            verticalAlignment = Alignment.CenterVertically
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
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // КОЛИЧЕСТВО ВСТРЕЧ
                        Text(
                            text = "Количество встреч:",
                            color = Color(0xFF8338EB),
                            fontFamily = mulishFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            MeetingsCountDropdown(
                                value = viewModel.meetingsCount,
                                onValueChange = { viewModel.meetingsCount = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AddStreamMarket(viewModel = viewModel)

                    Spacer(modifier = Modifier.height(14.dp))

                    viewModel.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    StreamButton(
                        text = if (viewModel.isLoading) "Создание..." else "Создать поток",
                        onClick = { viewModel.createStream(onSuccess = onSuccess) }
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}