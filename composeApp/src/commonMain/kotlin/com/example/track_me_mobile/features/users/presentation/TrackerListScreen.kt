package com.example.track_me_mobile.features.tracker_list.presentation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.ui.components.MainTopHeader

import com.example.track_me_mobile.features.tracker_list.domain.models.TrackerUser
import com.example.track_me_mobile.features.tracker_list.presentation.components.SearchBar
import com.example.track_me_mobile.features.tracker_list.presentation.components.TrackerItem

@Composable
fun TrackerListScreen(
    viewModel: TrackerListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    TrackerListContent(
        state = state,
        onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
        onDeleteUser = { viewModel.deleteUser(it) },
        onConfirmUser = { /* логика подтверждения */ }
    )
}
@Composable
fun TrackerListContent(
    state: TrackerListState,
    onSearchQueryChange: (String) -> Unit,
    onDeleteUser: (String) -> Unit,
    onConfirmUser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Используем Box как главный контейнер для наслоения элементов
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F4FF))
    ) {
        // 1. КОНТЕНТ (Нижний слой)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Отступ, равный высоте хедера (100.dp), чтобы контент не залез ПОД фиолетовую панель
            Spacer(Modifier.height(80.dp))

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "← Трекеры",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF44069A)
                )
            }

            Spacer(Modifier.height(16.dp))

            SearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.users) { user ->
                    TrackerItem(
                        user = user,
                        onConfirm = { onConfirmUser(user.id) },
                        onDelete = { onDeleteUser(user.id) }
                    )
                }
            }
        }

        MainTopHeader()

    }
}

// Провайдер для превью
class TrackerListPreviewParameterProvider : PreviewParameterProvider<TrackerListState> {
    override val values: Sequence<TrackerListState> = sequenceOf(
        TrackerListState(
            users = listOf(
                TrackerUser("1", "Иванов Иван Иванович", "ivan_tg", true),
                TrackerUser("2", "Петров Петр Петрович", "petr_tg", false),
                TrackerUser("3", "Сидоров Сидор", "sidor_tg", true),
                TrackerUser("4", "Смирнова Анна", "anna_news", true),
            ),
            searchQuery = ""
        ),
        TrackerListState(
            users = listOf(
                TrackerUser("1", "Иванов Иван Иванович", "ivan_tg", true),
                TrackerUser("2", "Петров Петр Петрович", "petr_tg", false),
            ),
            searchQuery = "иван"
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TrackerListScreenPreview(
    @PreviewParameter(TrackerListPreviewParameterProvider::class) state: TrackerListState
) {
    MaterialTheme {
        TrackerListContent(
            state = state,
            onSearchQueryChange = {},
            onDeleteUser = {},
            onConfirmUser = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrackMeHeader_Preview() {
    // Создаем тестовое состояние для поиска, если вы хотите видеть все вместе
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Фон всего экрана
    ) {


        // Тестовый контент под хедером
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Контент под хедером",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Если вы хотите проверить, как хедер смотрится с поиском:
            // SearchBar(query = searchText, onQueryChange = { searchText = it })
        }
    }
}