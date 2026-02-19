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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.ui.theme.MontserratFontFamily
import com.example.track_me_mobile.core.ui.components.TrackMeTextField
import com.example.track_me_mobile.core.ui.components.TrackMePasswordField
import com.example.track_me_mobile.generated.resources.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

// ── Вспомогательные функции и модели (оставлены без изменений) ──

data class PasswordRequirements(
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
    val hasSpecialChar: Boolean,
    val isMinLength: Boolean,
    val isMaxLength: Boolean
)

fun checkPassword(pass: String) = PasswordRequirements(
    hasUppercase    = pass.any { it.isUpperCase() },
    hasLowercase    = pass.any { it.isLowerCase() },
    hasDigit        = pass.any { it.isDigit() },
    hasSpecialChar  = pass.any { "@$!%*?&".contains(it) },
    isMinLength     = pass.length >= 6,
    isMaxLength     = pass.length <= 20
)

fun isEmailValid(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    return email.isNotEmpty() && emailRegex.matches(email)
}

fun isPhoneValid(phone: String): Boolean {
    return phone.length == 12 && phone.startsWith("+7")
}

fun Role.toRussian(): String = when (this) {
    Role.TRACKER     -> "Трекер"
    Role.ADMIN       -> "Администратор"
    Role.SUPER_ADMIN -> "Супер-администратор"
    Role.UNKNOWN     -> "Неизвестен"
}

// Маппинг Role → строка для API
fun Role.toApiString(): String = when (this) {
    Role.TRACKER     -> "TRACKER"
    Role.ADMIN       -> "ADMIN"
    Role.SUPER_ADMIN -> "SUPER_ADMIN"
    Role.UNKNOWN     -> "TRACKER"
}

@Composable
fun ErrorLabel(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color(0xFFFF7F7F), RoundedCornerShape(28.dp))
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

// ── Screen — точка входа из навигации ──
class RegistrationScreen : Screen {

    @Composable
    override fun Content() {
        val navigator  = LocalNavigator.currentOrThrow
        val viewModel  = koinScreenModel<RegistrationViewModel>()
        val state      = viewModel.state

        // Если успех — показываем экран "Проверьте почту"
        if (state is RegistrationState.Success) {
            RegistrationSuccessScreen(
                onBackToLogin = {
                    viewModel.resetState()
                    navigator.pop()
                }
            )
            return
        }

        RegistrationContent(
            isLoading    = state is RegistrationState.Loading,
            serverError  = (state as? RegistrationState.Error)?.message,
            onSubmit     = { username, password, phone, fullName, email, role ->
                viewModel.register(
                    username    = username,
                    password    = password,
                    phoneNumber = phone,
                    fullName    = fullName,
                    email       = email,
                    role        = role.toApiString()
                )
            }
        )
    }
}

// ── Экран "Проверьте почту" — показывается после успешной отправки ──
@Composable
fun RegistrationSuccessScreen(onBackToLogin: () -> Unit) {
    val montserrat = MontserratFontFamily()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Проверьте вашу почту",
                fontSize = 24.sp,
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF44069A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Мы отправили ссылку для подтверждения регистрации на вашу электронную почту. Перейдите по ссылке чтобы завершить регистрацию.",
                fontSize = 14.sp,
                fontFamily = montserrat,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF44069A),
                    contentColor = Color.White
                )
            ) {
                Text("Вернуться ко входу", fontFamily = montserrat, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Форма регистрации — разметка оригинальная, добавлены параметры ──
@Composable
fun RegistrationContent(
    isLoading: Boolean,
    serverError: String?,
    onSubmit: (username: String, password: String, phone: String, fullName: String, email: String, role: Role) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState  = rememberScrollState()
    val montserrat   = MontserratFontFamily()
    val density      = LocalDensity.current

    var fullName         by remember { mutableStateOf("") }
    var username         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var phone            by remember { mutableStateOf("+7") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var selectedRole     by remember { mutableStateOf<Role?>(null) }
    var isExpanded       by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var showErrors       by remember { mutableStateOf(false) }

    val availableRoles = listOf(Role.TRACKER, Role.ADMIN)

    val fullNameRegex = "^[a-zA-Zа-яА-ЯёЁ\\s-]+$".toRegex()
    val fullNameError = when {
        fullName.isEmpty()              -> "Введите ФИО"
        !fullNameRegex.matches(fullName) -> "Используйте только буквы"
        else -> null
    }

    val cleanTg            = username.removePrefix("@")
    val tgContentRegex     = "^[a-zA-Z0-9_]*$".toRegex()
    val telegramLengthValid = cleanTg.length in 5..32
    val telegramContentValid = tgContentRegex.matches(cleanTg)
    val telegramError = when {
        cleanTg.isEmpty()        -> "Введите имя пользователя"
        !telegramLengthValid     -> "Username должен быть от 5 до 32 символов"
        !telegramContentValid    -> "Только латинские буквы, цифры и _"
        else -> null
    }

    val requirements  = checkPassword(password)
    val allMet        = requirements.run { hasUppercase && hasLowercase && hasDigit && hasSpecialChar && isMinLength && isMaxLength }
    val passwordsMatch = password == confirmPassword && password.isNotEmpty()
    val emailValid    = isEmailValid(email)
    val phoneValid    = isPhoneValid(phone)
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    val formValid = fullNameError == null && telegramError == null &&
            allMet && passwordsMatch && emailValid && phoneValid && selectedRole != null

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
        Text("Регистрация", fontSize = 32.sp, fontFamily = montserrat, color = Color(0xFF44069A))
        Spacer(modifier = Modifier.height(25.dp))
        AvatarUploadBlock()
        Spacer(modifier = Modifier.height(32.dp))

        // Роль
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFF44069A), RoundedCornerShape(28.dp))
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(selectedRole?.toRussian() ?: "Роль", color = Color(0xFF44069A), fontFamily = montserrat, fontSize = 14.sp)
                        Icon(painterResource(Res.drawable.arrowup), null, Modifier.size(20.dp).rotate(arrowRotation), Color(0xFF44069A))
                    }
                }
                if (isExpanded) {
                    Popup(alignment = Alignment.TopCenter, offset = IntOffset(0, 160), onDismissRequest = { isExpanded = false }) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(0.88f).shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFF44069A))
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                availableRoles.forEach { role ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clickable { selectedRole = role; isExpanded = false }
                                            .padding(vertical = 12.dp, horizontal = 20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("▶", color = Color(0xFF44069A), fontSize = 10.sp)
                                        Spacer(Modifier.width(12.dp))
                                        Text(role.toRussian(), color = Color(0xFF44069A), fontFamily = montserrat, fontSize = 14.sp)
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
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMeTextField(value = fullName, onValueChange = { fullName = it }, label = "ФИО")
            if (showErrors && fullNameError != null) ErrorLabel(fullNameError)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMeTextField(value = username, onValueChange = { username = it }, label = "Имя пользователя (username)")
            if (showErrors && telegramError != null) ErrorLabel(telegramError)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMeTextField(value = email, onValueChange = { email = it }, label = "E-mail", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            if (showErrors && !emailValid) ErrorLabel("Введите корректный адрес почты")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMeTextField(
                value = phone,
                onValueChange = { input -> if (input.startsWith("+7") && input.length <= 12) phone = input },
                label = "Телефон",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            if (showErrors && !phoneValid) ErrorLabel("Введите корректный номер (+7XXXXXXXXXX)")
        }

        Spacer(modifier = Modifier.height(16.dp))
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
                        Surface(
                            modifier = Modifier.fillMaxWidth(0.9f).shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFF44069A))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                listOf(
                                    "заглавная буква" to requirements.hasUppercase,
                                    "строчная буква" to requirements.hasLowercase,
                                    "цифра" to requirements.hasDigit,
                                    "специальный символ (@\$!%*?&)" to requirements.hasSpecialChar,
                                    "длина: 6–20 символов" to (requirements.isMinLength && requirements.isMaxLength)
                                ).forEach { (text, met) ->
                                    val color = if (met) Color(0xFF2E7D32) else Color(0xFF44069A)
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                                        Text(if (met) "✓ " else "• ", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text, color = color, fontSize = 12.sp, fontFamily = montserrat)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showErrors && !allMet) ErrorLabel("Некорректный пароль")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMePasswordField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Повторите пароль")
            if (showErrors && !passwordsMatch) ErrorLabel("Пароли не совпадают")
        }

        // Ошибка с сервера
        serverError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            ErrorLabel(it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF44069A))
        } else {
            Button(
                onClick = {
                    if (formValid) {
                        focusManager.clearFocus()
                        onSubmit(username, password, phone, fullName, email, selectedRole!!)
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
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AvatarUploadBlock() {
    Box(modifier = Modifier.size(140.dp).clickable { }, contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(Color(0xFFEADDFF)))
        Icon(painterResource(Res.drawable.man), null, Modifier.size(90.dp), Color(0xFF8338EB))
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 12.dp)) {
            Icon(painterResource(Res.drawable.download), null, Modifier.size(17.dp), Color(0xFF8338EB))
        }
    }
}