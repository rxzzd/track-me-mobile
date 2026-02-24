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
import com.example.track_me_mobile.generated.resources.Mulish_SemiBold
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.go_back_icon
import org.jetbrains.compose.resources.painterResource

class AddStreamScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<AddStreamViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        AddStreamPageContent(
            viewModel = viewModel,
            onSuccess = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStreamPageContent(
    viewModel: AddStreamViewModel,
    onSuccess: () -> Unit
) {
    val navigator = LocalNavigator.currentOrThrow
    val mulishFamily = FontFamily(
        Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold)
    )

    MaterialTheme {
        Scaffold(
            topBar = {
                MainTopHeader()
            },
            containerColor = Color(0xFFF8F3FF)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F3FF))
            ) {
                IconButton(onClick = { navigator.push(StreamListScreen()) }) {
                    Icon(
                        painter = painterResource(Res.drawable.go_back_icon),
                        contentDescription = "Go back",
                        tint = Color(0xFF8338EB)
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Создание потока",
                        fontSize = 32.sp,
                        color = Color(0xFF44069A),
                        fontFamily = mulishFamily,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 32.sp,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AddStreamPhoto()

                    Spacer(modifier = Modifier.height(20.dp))

                    StreamNameInput(
                        value = viewModel.name,
                        onValueChange = { viewModel.name = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // НАЧАЛО ПОТОКА
                    Row(
                        modifier = Modifier.width(220.dp).wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Начало:",
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
                        modifier = Modifier.width(220.dp).wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Конец:",
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

                    // НОВОЕ ПОЛЕ: ДАТА НАЧАЛА ТРЕКШЕН-МИТИНГА
                    Row(
                        modifier = Modifier.width(220.dp).wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
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

                    Spacer(modifier = Modifier.height(9.dp))

                    AddStreamMarket(viewModel = viewModel)

                    Spacer(modifier = Modifier.height(14.dp))

                    // НОВОЕ ПОЛЕ: КОЛИЧЕСТВО ВСТРЕЧ
                    Column(
                        modifier = Modifier.width(220.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Количество встреч:",
                            color = Color(0xFF8338EB),
                            fontFamily = mulishFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        MeetingsCountDropdown(
                            value = viewModel.meetingsCount,
                            onValueChange = { viewModel.meetingsCount = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                        onClick = {
                            viewModel.createStream(onSuccess = onSuccess)
                        }
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}