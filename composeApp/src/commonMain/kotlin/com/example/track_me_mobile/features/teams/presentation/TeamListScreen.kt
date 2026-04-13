package com.example.track_me_mobile.features.teams.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import kotlin.math.roundToInt
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import cafe.adriel.voyager.koin.koinScreenModel
import com.example.track_me_mobile.core.ui.theme.MontserratFontFamily
import com.example.track_me_mobile.core.ui.utils.NavigationRefreshEffect
import com.example.track_me_mobile.features.streams.presentation.components.FeedbackFab
import com.example.track_me_mobile.features.team_card.presentation.CreateTeamLevel
import com.example.track_me_mobile.features.team_card.presentation.EditTeamLevel
import com.example.track_me_mobile.features.team_card.presentation.InfoTeamLevel
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
import com.example.track_me_mobile.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role

val PrimaryPurple     = Color(0xFF8338EB)
val DarkPurple        = Color(0xFF44069A)
val LightPurpleBg     = Color(0xFFF0E5FF)
val SearchBarBg       = Color(0xFFCDAFF7)
val StatusGreen       = Color(0xFF0DB862)
val StatusGray        = Color(0xFF878685)
val FilterModalBg     = Color(0xFFD7C7FF)
val ProjectLabelColor = Color(0xFF8338EB)

data class TrlOption(val label: String, val range: IntRange)

val trlOptions = listOf(
    TrlOption("0-2",  0..2),
    TrlOption("3-5",  3..5),
    TrlOption("6-8",  6..8),
    TrlOption("9-10", 9..10)
)


data class TeamListScreen(
    val streamId: String? = null
) : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<TeamListViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) { viewModel.initialize(streamId) }

        val isTopScreen = navigator.lastItem == this
        LaunchedEffect(isTopScreen) {
            if (isTopScreen) viewModel.loadTeams()
        }

        TeamListContent(
            state          = viewModel.state,
            searchQuery    = viewModel.searchQuery,
            onSearchChange = viewModel::onSearchQueryChange,
            onFilterApply  = viewModel::onFilterApply,
            onRetry        = viewModel::loadTeams,
            onRefresh      = viewModel::loadTeams
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamListContent(
    state: TeamListState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onFilterApply: (List<String>, List<IntRange>) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    val navigator = LocalNavigator.currentOrThrow
    val montserrat = MontserratFontFamily()
    val userInfoHolder = koinInject<UserInfoHolder>()
    val userRole = userInfoHolder.userInfo?.mainRole

    var showFilters by remember { mutableStateOf(false) }
    val selectedMarkets = remember { mutableStateListOf<String>() }
    val selectedTrls    = remember { mutableStateListOf<TrlOption>() }
    val isRefreshing    = state is TeamListState.Loading

    Scaffold(
        topBar = { MainTopHeader() },
        containerColor = Color.White,
        floatingActionButton = { FeedbackFab() },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = onRefresh,
            modifier     = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(horizontal = 20.dp)
            ) {

                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN) {
                        Icon(
                            painter = painterResource(Res.drawable.arrowback),
                            contentDescription = null,
                            tint = DarkPurple,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { navigator.pop() }
                        )
                        Spacer(Modifier.width(12.dp))
                    }

                    Text(
                        text = "Команды",
                        fontFamily = montserrat,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkPurple,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 49.dp, height = 40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(PrimaryPurple)
                            .clickable { showFilters = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.filter_icon1),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier.weight(1f).height(40.dp),
                        singleLine = true,
                        cursorBrush = SolidColor(DarkPurple),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = montserrat,
                            color = Color.White
                        ),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(SearchBarBg, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.icon_search),
                                    contentDescription = null,
                                    tint = DarkPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Найти",
                                            fontSize = 14.sp,
                                            fontFamily = montserrat,
                                            color = Color.White
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )

                    Spacer(Modifier.width(12.dp))

                    Icon(
                        painter = painterResource(Res.drawable.icon_plus),
                        contentDescription = "Создать команду",
                        tint = PrimaryPurple,
                        modifier = Modifier
                            .size(25.dp)
                            .clickable { navigator.push(CreateTeamLevel()) }
                    )
                }

                Spacer(Modifier.height(20.dp))

                when (val s = state) {

                    is TeamListState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryPurple)
                        }
                    }

                    is TeamListState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(s.message, color = Color.Gray, fontFamily = montserrat)
                                Spacer(Modifier.height(12.dp))
                                Button(onClick = onRetry) {
                                    Text("Повторить", fontFamily = montserrat)
                                }
                            }
                        }
                    }

                    is TeamListState.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(s.teams) { team ->
                                TeamCard(team)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        Dialog(onDismissRequest = { showFilters = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = FilterModalBg,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                FilterDialogContent(
                    selectedMarkets = selectedMarkets,
                    selectedTrls    = selectedTrls,
                    onDismiss = {
                        showFilters = false
                        onFilterApply(
                            selectedMarkets.toList(),
                            selectedTrls.map { it.range }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun FilterDialogContent(
    selectedMarkets: SnapshotStateList<String>,
    selectedTrls: SnapshotStateList<TrlOption>,
    onDismiss: () -> Unit
) {
    val montserrat = MontserratFontFamily()
    val markets = listOf(
        "AutoNet", "HealthNet", "MariNet", "NeuroNet", "SafeNet",
        "FoodNet", "EnergyNet", "WearNet", "AeroNet", "EduNet",
        "GameNet", "EcoNet", "HomeNet", "SportNet"
    )

    Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Рынки НТИ",
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(painterResource(Res.drawable.ic_close), null, Modifier.size(20.dp), Color.White)
            }
        }

        Spacer(Modifier.height(12.dp))

        Column {
            markets.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { market ->
                        Row(
                            Modifier.weight(1f).padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomCheckbox(
                                checked = selectedMarkets.contains(market),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) selectedMarkets.add(market)
                                    else selectedMarkets.remove(market)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(market, fontSize = 13.sp, fontWeight = FontWeight.W600, fontFamily = montserrat, color = Color.Black)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("TRL", fontFamily = montserrat, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)

        trlOptions.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                CustomCheckbox(
                    checked = selectedTrls.contains(option),
                    onCheckedChange = { isChecked ->
                        if (isChecked) selectedTrls.add(option)
                        else selectedTrls.remove(option)
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text(option.label, fontSize = 13.sp, fontFamily = montserrat, fontWeight = FontWeight.W600, color = Color.Black)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy((-12).dp)
        ) {
            TextButton(onClick = { selectedMarkets.clear(); selectedTrls.clear() }) {
                Text("Сбросить", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = montserrat, fontSize = 16.sp)
            }
            TextButton(onClick = onDismiss) {
                Text("Применить", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = montserrat, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun TeamCard(team: TeamCard) {
    val montserrat = MontserratFontFamily()
    val navigator  = LocalNavigator.currentOrThrow

    var isExpanded by remember { mutableStateOf(false) }

    val MAX_DESCRIPTION_LENGTH = 100
    val needsExpansion = team.description.length > MAX_DESCRIPTION_LENGTH

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(LightPurpleBg)
            .clickable { navigator.push(InfoTeamLevel(team.id)) }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    Image(
                        painter = painterResource(Res.drawable.base_stream_photo),
                        contentDescription = "Фото потока",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                team.averageGrade?.let { avg ->
                    val gradeColor = when {
                        avg >= 0.51 -> Color(0xB30DB862)
                        avg >= 0.26 -> Color(0xCCFFC411)
                        avg >= 0.0  -> Color(0xCCFF1504)
                        else -> Color(0xFF878685)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = -55.dp, y = (11).dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(gradeColor)
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = avg.formatTwoDecimals(),
                            color = Color.Black,
                            fontFamily = montserrat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(width = 89.dp, height = 14.dp)
                        .background(
                            color = if (team.enabled) StatusGreen else StatusGray,
                            shape = RoundedCornerShape(7.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (team.enabled) "Активна" else "Неактивна",
                        color = Color.White,
                        modifier = Modifier.offset(y = (-1).dp),
                        style = TextStyle(
                            fontFamily = montserrat,
                            fontWeight = FontWeight.W700,
                            fontSize = 12.sp,
                            letterSpacing = 0.sp,
                            lineHeight = 12.sp
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = team.name,
                        fontFamily = montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(Res.drawable.first_pencil),
                        contentDescription = null,
                        modifier = Modifier
                            .size(15.dp)
                            .clickable { navigator.push(EditTeamLevel(team.id)) },
                        tint = DarkPurple
                    )
                }

                Spacer(Modifier.height(4.dp))

                Column {
                    val displayText = if (needsExpansion && !isExpanded) {
                        team.description.take(MAX_DESCRIPTION_LENGTH) + "..."
                    } else {
                        team.description
                    }

                    Text(
                        text = displayText,
                        fontFamily = montserrat,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color.Black
                    )

                    if (needsExpansion) {
                        Text(
                            text = if (isExpanded) "Скрыть" else "Подробнее...",
                            fontFamily = montserrat,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable(
                                    onClick = { isExpanded = !isExpanded },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val labelStyle = TextStyle(
                        fontFamily = montserrat,
                        fontSize = 11.sp,
                        color = ProjectLabelColor
                    )
                    Text("Рынки НТИ: ${team.ntiMarkets.joinToString(", ") { it.displayName }}", style = labelStyle)
                    Text("TRL: ${team.readinessLevel}", style = labelStyle)
                    Text("Поток: ${team.stream?.name ?: "—"}", style = labelStyle)
                }
            }
        }
    }
}

private fun Double.formatTwoDecimals(): String {
    val rounded = (this * 100.0).roundToInt() / 100.0
    val parts = rounded.toString().split('.')
    return if (parts.size == 1) {
        "${parts[0]}.00"
    } else {
        val frac = parts[1].padEnd(2, '0').take(2)
        "${parts[0]}.$frac"
    }
}

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .border(1.dp, Color.White, RoundedCornerShape(4.dp))
            .background(if (checked) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                painter = painterResource(Res.drawable.icon_check),
                contentDescription = null,
                tint = DarkPurple,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}