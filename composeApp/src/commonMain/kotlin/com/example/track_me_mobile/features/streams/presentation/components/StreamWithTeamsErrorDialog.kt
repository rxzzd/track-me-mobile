package com.example.track_me_mobile.features.streams.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
import org.jetbrains.compose.resources.Font
import com.example.track_me_mobile.generated.resources.Mulish_SemiBold
import com.example.track_me_mobile.generated.resources.Res

@Composable
fun StreamWithTeamsErrorDialog(
    teams: List<TeamCard>,
    onDismiss: () -> Unit,
    onTeamClick: (String) -> Unit // teamId -> navigate to team screen
) {
    val mulishFamily = FontFamily(
        Font(Res.font.Mulish_SemiBold, FontWeight.SemiBold)
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .border(2.dp, Color(0xFFE57373), RoundedCornerShape(28.dp))
                .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Кнопка закрытия
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

                Spacer(modifier = Modifier.height(8.dp))

                // Заголовок
                Text(
                    text = "Невозможно удалить поток",
                    textAlign = TextAlign.Center,
                    fontFamily = mulishFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE57373),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Описание
                Text(
                    text = "В потоке есть команды:",
                    textAlign = TextAlign.Center,
                    fontFamily = mulishFamily,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Список команд (скроллируемый)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 300.dp)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(teams) { team ->
                        TeamListItem(
                            team = team,
                            onClick = { onTeamClick(team.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Подсказка
                Text(
                    text = "Удалите команды перед удалением потока",
                    textAlign = TextAlign.Center,
                    fontFamily = mulishFamily,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TeamListItem(
    team: TeamCard,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = team.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF44069A)
            )
            Text(
                text = "@${team.username}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Go to team",
            tint = Color(0xFF44069A),
            modifier = Modifier.size(20.dp)
        )
    }
}