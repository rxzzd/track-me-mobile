package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource

// Импорт твоего Enum
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.man
import com.example.track_me_mobile.generated.resources.download
import androidx.compose.ui.tooling.preview.Preview

import com.example.track_me_mobile.core.ui.components.TrackMeTextField
import com.example.track_me_mobile.core.ui.components.TrackMePasswordField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.track_me_mobile.core.ui.theme.MontserratFontFamily

// Маппинг для отображения ролей в UI
fun Role.toRussian(): String = when (this) {
    Role.TRACKER -> "Трекер"
    Role.ADMIN -> "Администратор"
    Role.SUPER_ADMIN -> "Супер-администратор"
}

@Composable
fun RegistrationScreen() {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val montserrat = MontserratFontFamily()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var telegram by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Теперь используем тип Role вместо String
    var selectedRole by remember { mutableStateOf<Role?>(null) }
    var isExpanded by remember { mutableStateOf(false) }

    // Список доступных для выбора ролей (без Super Admin для регистрации)
    val availableRoles = listOf(Role.TRACKER, Role.ADMIN)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
            .clickable { focusManager.clearFocus() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "Регистрация",
            fontSize = 32.sp,
            fontFamily = montserrat,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF44069A)
        )

        Spacer(modifier = Modifier.height(25.dp))

        AvatarUploadBlock()

        Spacer(modifier = Modifier.height(32.dp))

        // Поле выбора роли, привязанное к Enum
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedRole?.toRussian() ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text("Роль", color = Color(0xFF44069A), fontFamily = montserrat)
                },
                trailingIcon = {
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        color = Color(0xFF44069A),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF44069A),
                    unfocusedBorderColor = Color(0xFF44069A),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { isExpanded = !isExpanded }
            )

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color.White)
            ) {
                availableRoles.forEach { role ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("▶", color = Color(0xFF44069A), fontSize = 10.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = role.toRussian(),
                                    color = Color(0xFF44069A),
                                    fontFamily = montserrat
                                )
                            }
                        },
                        onClick = {
                            selectedRole = role
                            isExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TrackMeTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = "ФИО"
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrackMeTextField(
            value = email,
            onValueChange = { email = it },
            label = "E-mail",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrackMeTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Телефон",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrackMeTextField(
            value = telegram,
            onValueChange = { telegram = it },
            label = "Имя пользователя telegram"
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrackMePasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Пароль"
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrackMePasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Повторите пароль"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Здесь ты можешь собрать объект CurrentUser, используя selectedRole
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF44069A),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Зарегистрироваться",
                fontSize = 16.sp,
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AvatarUploadBlock() {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clickable { /* Логика выбора фото */ },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEADDFF))
        )

        Icon(
            painter = painterResource(Res.drawable.man),
            contentDescription = null,
            modifier = Modifier.size(90.dp),
            tint = Color(0xFF8338EB)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.download),
                contentDescription = "Загрузить",
                modifier = Modifier.size(17.dp),
                tint = Color(0xFF8338EB)
            )
        }
    }
}

@Preview
@Composable
fun RegistrationPagePreview() {
    RegistrationScreen()
}