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

class AddStreamScreen : Screen {

    @Composable
    override fun Content() {
        // Получаем ViewModel через Koin
        val viewModel = koinScreenModel<AddStreamViewModel>()
        // Получаем навигатор для возврата назад
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

                    Spacer(modifier = Modifier.height(9.dp))

                    AddStreamMarket()

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
                        },
                        // Можно добавить enabled = !viewModel.isLoading, если компонент поддерживает
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}