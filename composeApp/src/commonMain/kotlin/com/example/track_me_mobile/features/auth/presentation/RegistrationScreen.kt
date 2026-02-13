package com.example.track_me_mobile.features.auth.presentation

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.onFocusChanged
import org.jetbrains.compose.resources.painterResource

import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.ui.theme.MontserratFontFamily
import com.example.track_me_mobile.core.ui.components.TrackMeTextField
import com.example.track_me_mobile.core.ui.components.TrackMePasswordField
import com.example.track_me_mobile.generated.resources.*

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

data class PasswordRequirements(
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
    val hasSpecialChar: Boolean,
    val isMinLength: Boolean
)

fun checkPassword(pass: String) = PasswordRequirements(
    hasUppercase = pass.any { it.isUpperCase() },
    hasLowercase = pass.any { it.isLowerCase() },
    hasDigit = pass.any { it.isDigit() },
    hasSpecialChar = pass.any { "@$!%*?&".contains(it) },
    isMinLength = pass.length >= 6
)

fun isEmailValid(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    return email.isNotEmpty() && emailRegex.matches(email)
}

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

    var selectedRole by remember { mutableStateOf<Role?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    val availableRoles = listOf(Role.TRACKER, Role.ADMIN)

    var isPasswordFocused by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val requirements = checkPassword(password)
    val allMet = requirements.run { hasUppercase && hasLowercase && hasDigit && hasSpecialChar && isMinLength }
    val passwordsMatch = password == confirmPassword && password.isNotEmpty()
    val emailValid = isEmailValid(email)

    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 0f else 180f)

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
            color = Color(0xFF44069A)
        )

        Spacer(modifier = Modifier.height(25.dp))

        AvatarUploadBlock()

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.fillMaxWidth().zIndex(2f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFFFFFFF).copy(alpha = 0.5f))
                    .border(1.dp, Color(0xFF44069A), RoundedCornerShape(28.dp))
                    .clickable { isExpanded = !isExpanded }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedRole?.toRussian() ?: "Роль",
                        color = Color(0xFF44069A),
                        fontFamily = montserrat,
                        fontSize = 14.sp
                    )
                    Icon(
                        painter = painterResource(Res.drawable.arrowup),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(arrowRotation),
                        tint = Color(0xFF44069A)
                    )
                }

                if (isExpanded) {
                    availableRoles.forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRole = role
                                    isExpanded = false
                                }
                                .padding(vertical = 12.dp, horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("▶", color = Color(0xFF44069A), fontSize = 10.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = role.toRussian(),
                                color = Color(0xFF44069A),
                                fontFamily = montserrat,
                                fontSize = 14.sp
                            )
                        }
                    }
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

        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMeTextField(
                value = email,
                onValueChange = {
                    email = it
                    showErrors = false
                },
                label = "E-mail",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            if (showErrors && !emailValid) {
                Text(
                    text = "Введите корректный адрес почты",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp),
                    fontFamily = montserrat
                )
            }
        }

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

        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMePasswordField(
                value = password,
                onValueChange = {
                    password = it
                    showErrors = false
                },
                label = "Пароль",
                modifier = Modifier.onFocusChanged { isPasswordFocused = it.isFocused }
            )

            if ((isPasswordFocused || showErrors) && !allMet) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF8338EB))
                        .padding(16.dp)
                ) {
                    val lines = listOf(
                        "пароль должен содержать заглавную букву" to requirements.hasUppercase,
                        "пароль должен содержать строчную букву" to requirements.hasLowercase,
                        "пароль должен содержать цифру" to requirements.hasDigit,
                        "пароль должен содержать специальный символ (@$!%*?&)" to requirements.hasSpecialChar,
                        "длина пароля должна быть не менее 6 символов" to requirements.isMinLength
                    )

                    lines.forEach { (text, met) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "• ",
                                color = if (met) Color.White.copy(alpha = 0.5f) else Color.Yellow,
                                fontSize = 12.sp
                            )
                            Text(
                                text = text,
                                color = if (met) Color.White.copy(alpha = 0.5f) else Color.Yellow,
                                fontSize = 12.sp,
                                fontFamily = montserrat
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            TrackMePasswordField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    showErrors = false
                },
                label = "Повторите пароль"
            )

            if (showErrors && !passwordsMatch && confirmPassword.isNotEmpty()) {
                Text(
                    text = "Пароли не совпадают",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp),
                    fontFamily = montserrat
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (allMet && passwordsMatch && emailValid && selectedRole != null) {
                    focusManager.clearFocus()
                } else {
                    showErrors = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF44069A),
                contentColor = Color.White
            )
        ) {
            Text("Зарегистрироваться", fontFamily = montserrat, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AvatarUploadBlock() {
    Box(
        modifier = Modifier.size(140.dp).clickable { },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(Color(0xFFEADDFF)))
        Icon(
            painter = painterResource(Res.drawable.man),
            contentDescription = null,
            modifier = Modifier.size(90.dp),
            tint = Color(0xFF8338EB)
        )
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 12.dp)) {
            Icon(
                painter = painterResource(Res.drawable.download),
                contentDescription = null,
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