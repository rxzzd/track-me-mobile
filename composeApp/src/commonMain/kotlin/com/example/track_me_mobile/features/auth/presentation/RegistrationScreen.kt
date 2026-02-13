package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.jetbrains.compose.resources.painterResource

import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.ui.theme.MontserratFontFamily
import com.example.track_me_mobile.core.ui.components.TrackMeTextField
import com.example.track_me_mobile.core.ui.components.TrackMePasswordField
import com.example.track_me_mobile.generated.resources.*

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

// --- Вспомогательные модели и валидация ---

data class PasswordRequirements(
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
    val hasSpecialChar: Boolean,
    val isMinLength: Boolean,
    val isMaxLength: Boolean
)

fun checkPassword(pass: String) = PasswordRequirements(
    hasUppercase = pass.any { it.isUpperCase() },
    hasLowercase = pass.any { it.isLowerCase() },
    hasDigit = pass.any { it.isDigit() },
    hasSpecialChar = pass.any { "@$!%*?&".contains(it) },
    isMinLength = pass.length >= 6,
    isMaxLength = pass.length <= 20
)

fun isEmailValid(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    return email.isNotEmpty() && emailRegex.matches(email)
}

fun isFullNameValid(name: String): Boolean {
    val nameRegex = "^[a-zA-Zа-яА-ЯёЁ\\s-]+$".toRegex()
    return name.isNotEmpty() && nameRegex.matches(name)
}

fun isPhoneValid(phone: String): Boolean {
    return phone.length == 12 && phone.startsWith("+7")
}

fun isTelegramValid(username: String): Boolean {
    val clean = username.removePrefix("@")
    val tgRegex = "^[a-zA-Z0-9_]{4,20}$".toRegex()
    return clean.isNotEmpty() && tgRegex.matches(clean)
}

fun Role.toRussian(): String = when (this) {
    Role.TRACKER -> "Трекер"
    Role.ADMIN -> "Администратор"
    Role.SUPER_ADMIN -> "Супер-администратор"
}

// --- Компонент ошибки в стиле капсулы ---

@Composable
fun ErrorLabel(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color(0xFFAC201B), RoundedCornerShape(28.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = MontserratFontFamily(),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

// --- Основной экран ---

@Composable
fun RegistrationScreen() {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val montserrat = MontserratFontFamily()
    val density = LocalDensity.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+7") }
    var telegram by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var selectedRole by remember { mutableStateOf<Role?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    val availableRoles = listOf(Role.TRACKER, Role.ADMIN)

    var isPasswordFocused by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    // Валидация
    val requirements = checkPassword(password)
    val allMet = requirements.run { hasUppercase && hasLowercase && hasDigit && hasSpecialChar && isMinLength && isMaxLength }

    // Пароли совпадают, если они идентичны и первый пароль не пустой
    val passwordsMatch = password == confirmPassword && password.isNotEmpty()

    val emailValid = isEmailValid(email)
    val fullNameValid = isFullNameValid(fullName)
    val phoneValid = isPhoneValid(phone)
    val telegramValid = isTelegramValid(telegram)

    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

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
        Text(text = "Регистрация", fontSize = 32.sp, fontFamily = montserrat, color = Color(0xFF44069A))
        Spacer(modifier = Modifier.height(25.dp))
        AvatarUploadBlock()
        Spacer(modifier = Modifier.height(32.dp))

        // Роль
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFF44069A), RoundedCornerShape(28.dp))
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = selectedRole?.toRussian() ?: "Роль", color = Color(0xFF44069A), fontFamily = montserrat, fontSize = 14.sp)
                        Icon(painter = painterResource(Res.drawable.arrowup), contentDescription = null, modifier = Modifier.size(20.dp).rotate(arrowRotation), tint = Color(0xFF44069A))
                    }
                }
                if (isExpanded) {
                    Popup(alignment = Alignment.TopCenter, offset = IntOffset(0, 160), onDismissRequest = { isExpanded = false }) {
                        Surface(modifier = Modifier.fillMaxWidth(0.88f).shadow(8.dp, RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFF44069A))) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                availableRoles.forEach { role ->
                                    Row(modifier = Modifier.fillMaxWidth().clickable { selectedRole = role; isExpanded = false }.padding(vertical = 12.dp, horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("▶", color = Color(0xFF44069A), fontSize = 10.sp)
                                        Spacer(Modifier.width(12.dp))
                                        Text(text = role.toRussian(), color = Color(0xFF44069A), fontFamily = montserrat, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showErrors && selectedRole == null) ErrorLabel("Выберите роль")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ФИО
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMeTextField(value = fullName, onValueChange = { fullName = it }, label = "ФИО")
            if (showErrors && !fullNameValid) ErrorLabel("Используйте только буквы (рус/eng)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // E-mail
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMeTextField(value = email, onValueChange = { email = it }, label = "E-mail", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            if (showErrors && !emailValid) ErrorLabel("Введите корректный адрес почты")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Телефон
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMeTextField(
                value = phone,
                onValueChange = { input ->
                    if (!input.startsWith("+7")) phone = "+7"
                    else {
                        val digits = input.substring(2).filter { it.isDigit() }
                        if (digits.length <= 10) phone = "+7$digits"
                    }
                },
                label = "Телефон",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            if (showErrors && !phoneValid) ErrorLabel("Введите корректный номер (+7XXXXXXXXXX)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Telegram
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMeTextField(
                value = telegram,
                onValueChange = { input ->
                    if (input.all { it.isLetterOrDigit() || it == '_' || it == '@' }) telegram = input
                },
                label = "Имя пользователя telegram"
            )
            if (showErrors && !telegramValid) ErrorLabel("Telegram ID должен быть от 4 до 20 символов")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Пароль
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                TrackMePasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    modifier = Modifier.onFocusChanged { isPasswordFocused = it.isFocused }
                )
                if (isPasswordFocused && !allMet) {
                    val popupOffsetY = with(density) { -320.dp.roundToPx() }
                    Popup(alignment = Alignment.TopCenter, offset = IntOffset(0, popupOffsetY)) {
                        Surface(modifier = Modifier.fillMaxWidth(0.9f).shadow(8.dp, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), color = Color(0xFF8338EB)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val lines = listOf(
                                    "пароль должен содержать заглавную букву" to requirements.hasUppercase,
                                    "пароль должен содержать строчную букву" to requirements.hasLowercase,
                                    "пароль должен содержать цифру" to requirements.hasDigit,
                                    "пароль должен содержать специальный символ (@$!%*?&)" to requirements.hasSpecialChar,
                                    "длина пароля: 6-20 символов" to (requirements.isMinLength && requirements.isMaxLength)
                                )
                                lines.forEach { (text, met) ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(text = "• ", color = if (met) Color.White.copy(alpha = 0.5f) else Color.Yellow, fontSize = 12.sp)
                                        Text(text = text, color = if (met) Color.White.copy(alpha = 0.5f) else Color.Yellow, fontSize = 12.sp, fontFamily = montserrat)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showErrors && !allMet) ErrorLabel("Некорректный пароль (6-20 символов)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Повторите пароль
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMePasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Повторите пароль"
            )
            if (showErrors && !passwordsMatch) {
                ErrorLabel("Неправильный пароль")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (allMet && passwordsMatch && emailValid && fullNameValid && phoneValid && telegramValid && selectedRole != null) {
                    focusManager.clearFocus()
                } else {
                    showErrors = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44069A), contentColor = Color.White)
        ) {
            Text("Зарегистрироваться", fontFamily = montserrat, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AvatarUploadBlock() {
    Box(modifier = Modifier.size(140.dp).clickable { }, contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(Color(0xFFEADDFF)))
        Icon(painter = painterResource(Res.drawable.man), contentDescription = null, modifier = Modifier.size(90.dp), tint = Color(0xFF8338EB))
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 12.dp)) {
            Icon(painter = painterResource(Res.drawable.download), contentDescription = null, modifier = Modifier.size(17.dp), tint = Color(0xFF8338EB))
        }
    }
}

@Preview
@Composable
fun RegistrationPagePreview() {
    RegistrationScreen()
}