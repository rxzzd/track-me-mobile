package com.example.track_me_mobile.features.users.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track_me_mobile.features.users.domain.models.AdminUser
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.edit
import com.example.track_me_mobile.generated.resources.icon_false
import com.example.track_me_mobile.generated.resources.icon_true
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminItem(
    user: AdminUser,
    onConfirm: () -> Unit,
    onDelete: () -> Unit,
    onProfileClick: () -> Unit
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    val purpleColor = Color(0xFF44069A)
    val greenColor = Color(0xFF00C853)
    val redColor = Color(0xFFD50000)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(2.dp, purpleColor, RoundedCornerShape(50))
            .background(Color.White, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .combinedClickable(
                onClick = { onProfileClick() },
                onLongClick = { isMenuOpen = !isMenuOpen }
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Аватарка (заглушка)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE0E0E0), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                user.fullName?.let {
                    Text(
                        text = it,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
                Text(
                    text = "@${user.telegramNick}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (isMenuOpen) {
                // Меню подтверждения/удаления
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .border(1.5.dp, purpleColor, RoundedCornerShape(50))
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onConfirm(); isMenuOpen = false },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.icon_true),
                            contentDescription = "Confirm",
                            tint = greenColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDelete(); isMenuOpen = false },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.icon_false),
                            contentDescription = "Delete",
                            tint = redColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                // Статус пользователя (когда меню закрыто)
                Box(modifier = Modifier.padding(end = 12.dp)) {
                    if (user.isConfirmed) {
                        Icon(
                            painter = painterResource(Res.drawable.icon_true),
                            contentDescription = "Confirmed",
                            tint = greenColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.edit),
                            contentDescription = "Pending",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}