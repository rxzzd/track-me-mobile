package com.example.track_me_mobile.features.users.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.features.profile.presentation.UserProfileScreen
import com.example.track_me_mobile.core.ui.components.MainTopHeader
import com.example.track_me_mobile.features.users.presentation.components.AdminItem
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.core.ui.theme.TrackMePurpleLight
import com.example.track_me_mobile.features.tracker_list.presentation.components.SearchBar
import org.koin.compose.koinInject

@Composable
fun AdminListScreen() {
    val viewModel: AdminListViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    AdminListContent(
        state = state,
        onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
        onDeleteUser = { viewModel.deleteUser(it) },
        onConfirmUser = { viewModel.confirmUser(it) },
        onToggleBlocked = { viewModel.toggleBlockedFilter() }
    )
}

@Composable
fun AdminListContent(
    state: AdminListState,
    onSearchQueryChange: (String) -> Unit,
    onDeleteUser: (String) -> Unit,
    onConfirmUser: (String) -> Unit,
    onToggleBlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.currentOrThrow
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F4FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(80.dp))
            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "← Администраторы",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrackMePurple
                )

                // ЗАМЕНЕНО: Кнопка с иконками Человек / Круг
                IconButton(
                    onClick = onToggleBlocked,
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (state.showBlocked) TrackMePurple else TrackMePurpleLight.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (state.showBlocked) Icons.Default.Circle else Icons.Default.Person,
                        contentDescription = "Фильтр",
                        tint = if (state.showBlocked) Color.White else TrackMePurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            SearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange
            )

            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TrackMePurple)
                    }
                }
                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.error, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { onToggleBlocked() }) { Text("Повторить") }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.users) { user ->
                            AdminItem(
                                user = user,
                                onConfirm = { onConfirmUser(user.telegramNick) },
                                onDelete = { onDeleteUser(user.telegramNick) },
                                onProfileClick = {
                                    navigator.push(UserProfileScreen(user.telegramNick))
                                }
                            )
                        }
                    }
                }
            }
        }
        MainTopHeader()
    }
}