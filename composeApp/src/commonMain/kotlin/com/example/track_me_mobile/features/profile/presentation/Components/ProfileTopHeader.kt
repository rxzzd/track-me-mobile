package com.example.track_me_mobile.features.profile.presentation.components

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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.track_me_mobile.core.ui.theme.TrackMePurple

class ProfileMenuShape(val density: Density) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val d = density.density
            val cp = 16f * d // Радиус внешних углов
            val innerCp = 15f * d // Радиус внутреннего закругления слева от кнопки
            val bw = 58f * d // Ширина кнопки
            val bh = 40f * d // Уменьшенная высота (приплюснутая кнопка)

            // Начало: Верхний левый угол основного блока
            moveTo(0f, bh + cp)
            quadraticBezierTo(0f, bh, cp, bh)

            // Линия к кнопке и ВНУТРЕННЕЕ закругление
            lineTo(size.width - bw - innerCp, bh)
            quadraticBezierTo(size.width - bw, bh, size.width - bw, bh - innerCp)

            // Подъем к кнопке
            lineTo(size.width - bw, cp)
            quadraticBezierTo(size.width - bw, 0f, size.width - bw + cp, 0f)

            // Верх кнопки
            lineTo(size.width - cp, 0f)
            quadraticBezierTo(size.width, 0f, size.width, cp)

            // Правая сторона и низ
            lineTo(size.width, size.height - cp)
            quadraticBezierTo(size.width, size.height, size.width - cp, size.height)
            lineTo(cp, size.height)
            quadraticBezierTo(0f, size.height, 0f, size.height - cp)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun ProfileTopHeader() {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(TrackMePurple)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Логотип TrackMe
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

        // Кнопка в хедере
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 4.dp)) {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.Menu, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            if (expanded) {
                Popup(
                    alignment = Alignment.TopEnd,
                    onDismissRequest = { expanded = false },
                    offset = IntOffset(10, (15)) // Небольшая корректировка под статус-бар
                ) {
                    val density = LocalDensity.current
                    val shape = remember(density) { ProfileMenuShape(density) }

                    Column(
                        modifier = Modifier
                            .width(200.dp) // Чуть компактнее
                            .background(Color.White, shape)
                            .border(2.dp, TrackMePurple, shape)
                    ) {
                        // Приплюснутая область кнопки
                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .size(58.dp, 36.dp) // Высота совпадает с bh в Shape
                                .clickable { expanded = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Menu, null, tint = TrackMePurple, modifier = Modifier.size(28.dp))
                        }

                        // Список слов
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, top = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MenuTextItem("Личный кабинет") { expanded = false }
                            MenuTextItem("Трекеры") { expanded = false }
                            MenuTextItem("Потоки", isBold = true) { expanded = false }
                            MenuTextItem("Все команды") { expanded = false }
                            MenuTextItem("Выйти") { expanded = false }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuTextItem(text: String, isBold: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 3.dp), // Уплотненные отступы
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TrackMePurple,
            fontSize = 15.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}