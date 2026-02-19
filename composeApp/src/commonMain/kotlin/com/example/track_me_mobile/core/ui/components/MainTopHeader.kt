package com.example.track_me_mobile.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.features.profile.presentation.ProfileScreen
import com.example.track_me_mobile.core.navigation.TrackerListScreen
import com.example.track_me_mobile.core.navigation.AdminListScreen
import com.example.track_me_mobile.core.ui.models.HeaderMenuItem
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import com.example.track_me_mobile.features.streams.presentation.StreamListScreen
import com.example.track_me_mobile.features.teams.presentation.TeamListScreen
import org.koin.compose.koinInject

@Composable
fun MainTopHeader() {
    val viewModel: GlobalHeaderViewModel = koinInject()
    val userRole by viewModel.userRole.collectAsState()

    val navigator = LocalNavigator.currentOrThrow
    val currentScreen = navigator.lastItem

    var expanded by remember { mutableStateOf(false) }

    val menuItems = remember(userRole, currentScreen) {
        val list = mutableListOf<HeaderMenuItem>()

        fun navigate(target: Screen) {
            if (currentScreen!!::class != target::class) {
                navigator.push(target)
            }
            expanded = false
        }

        // 1. Личный кабинет
        list.add(HeaderMenuItem(
            text = "Личный кабинет",
            isSelected = currentScreen is ProfileScreen,
            action = { navigate(ProfileScreen()) }
        ))

        when (userRole) {
            Role.SUPER_ADMIN -> {
                list.add(HeaderMenuItem("Администраторы", currentScreen is AdminListScreen) {
                    navigate(AdminListScreen())
                })
                list.add(HeaderMenuItem("Трекеры", currentScreen is TrackerListScreen) {
                    navigate(TrackerListScreen())
                })
                list.add(HeaderMenuItem("Потоки", currentScreen is StreamListScreen) {
                    navigate(StreamListScreen())
                })
                list.add(HeaderMenuItem("Все команды", currentScreen is TeamListScreen) {
                    navigate(TeamListScreen())
                })
                list.add(HeaderMenuItem("Отчетность", false) { expanded = false })
            }
            Role.ADMIN -> {
                list.add(HeaderMenuItem("Трекеры", currentScreen is TrackerListScreen) {
                    navigate(TrackerListScreen())
                })
                list.add(HeaderMenuItem("Потоки", currentScreen is StreamListScreen) {
                    navigate(StreamListScreen())
                })
                list.add(HeaderMenuItem("Все команды", currentScreen is TeamListScreen) {
                    navigate(TeamListScreen())
                })
            }
            Role.TRACKER -> {
                list.add(HeaderMenuItem("Главный экран", currentScreen is TeamListScreen) {
                    navigate(TeamListScreen())
                })
            }
            else -> {}
        }

        list.add(HeaderMenuItem("Выйти", false) {
            viewModel.logout { /* логика */ }
            expanded = false
        })
        list
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(TrackMePurple)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Левая часть: Логотип
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("T", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("TrackMe", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }

        // Правая часть: Кнопка Меню
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 4.dp)) {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.Menu, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            if (expanded) {
                Popup(
                    alignment = Alignment.TopEnd,
                    onDismissRequest = { expanded = false },
                    offset = IntOffset(10, 15)
                ) {
                    val shape = ProfileMenuShape(LocalDensity.current)
                    Column(
                        modifier = Modifier
                            .width(200.dp)
                            .background(Color.White, shape)
                            .border(2.dp, TrackMePurple, shape)
                    ) {
                        // Верхняя плашка закрытия
                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .size(58.dp, 36.dp)
                                .clip(RoundedCornerShape(topEnd = 16.dp))
                                .clickable { expanded = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Menu, null, tint = TrackMePurple, modifier = Modifier.size(28.dp))
                        }

                        // Список пунктов меню
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            menuItems.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (item.isSelected) TrackMePurple.copy(alpha = 0.12f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            expanded = false
                                            item.action()
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.text,
                                        color = TrackMePurple,
                                        fontSize = 15.sp,
                                        fontWeight = if (item.isSelected || item.isBold) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}