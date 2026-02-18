package com.example.track_me_mobile.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.track_me_mobile.core.ui.theme.*
import com.example.track_me_mobile.features.profile.presentation.components.ProfileTopHeader

class ProfileEditScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Shared ViewModel — тот же экземпляр, что и в ProfileScreen
        val viewModel = koinScreenModel<ProfileViewModel>()
        val profile = viewModel.profile

        // Пока профиль не загружен — показываем загрузку
        if (profile == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TrackMePurple)
            }
            return
        }

        ProfileEditScreenContent(
            initialName     = profile.fullName,
            initialEmail    = profile.email,
            initialPhone    = profile.phoneNumber ?: "+7",
            isSaving        = viewModel.isSaving,
            errorMessage    = viewModel.errorMessage,
            onSaveComplete  = { name, email, phone ->
                viewModel.saveProfile(name, email, phone) {
                    navigator.pop()
                }
            },
            onCancel = { navigator.pop() }
        )
    }
}

@Composable
fun ProfileEditScreenContent(
    initialName: String,
    initialEmail: String,
    initialPhone: String,
    isSaving: Boolean,
    errorMessage: String?,
    onSaveComplete: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var name     by remember { mutableStateOf(initialName) }
    var email    by remember { mutableStateOf(initialEmail) }
    var phone    by remember { mutableStateOf(initialPhone) }

    val isNameValid     = name.trim().split(" ").size >= 2
    val isEmailValid    = email.contains("@") && email.contains(".")
    val isPhoneValid    = phone.length == 12

    val isChanged = name != initialName || email != initialEmail || phone != initialPhone
    val canSave   = isNameValid && isEmailValid && isPhoneValid && isChanged && !isSaving

    Scaffold(
        topBar = { ProfileTopHeader() },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 34.dp, vertical = 35.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Личный кабинет", fontSize = 32.sp, color = TrackMePurple,
                modifier = Modifier.padding(bottom = 35.dp))

            Box(
                modifier = Modifier.size(180.dp).clip(RoundedCornerShape(20.dp))
                    .background(TrackMePurpleLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(120.dp), tint = TrackMePurple)
                IconButton(onClick = { }, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    Icon(Icons.Outlined.FileDownload, null, tint = TrackMePurple, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
            Text("Редактирование", color = TrackMePurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(25.dp))

            ProfileInputRow(
                value         = name,
                onValueChange = { if (it.all { c -> c.isLetter() || c.isWhitespace() }) name = it },
                isError       = !isNameValid,
                errorText     = "Введите фамилию и имя"
            )

            ProfileInputRow(
                value         = email,
                onValueChange = { email = it },
                isError       = !isEmailValid,
                errorText     = "Некорректный Email",
                keyboardType  = KeyboardType.Email
            )

            ProfileInputRow(
                value         = phone,
                onValueChange = { input ->
                    if (input.startsWith("+7")) {
                        val digits = input.substring(2).filter { it.isDigit() }
                        phone = "+7" + digits.take(10)
                    }
                },
                isError       = !isPhoneValid,
                errorText     = "Нужно 10 цифр после +7",
                keyboardType  = KeyboardType.Phone
            )

            // Показываем сетевую ошибку если есть
            errorMessage?.let {
                Text(
                    text     = it,
                    color    = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (isSaving) {
                CircularProgressIndicator(color = TrackMePurple)
            } else {
                Button(
                    onClick  = { onSaveComplete(name, email, phone) },
                    enabled  = canSave,
                    modifier = Modifier.fillMaxWidth(0.65f).height(46.dp),
                    shape    = RoundedCornerShape(50),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = TrackMePurple,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                    )
                ) {
                    Text("Сохранить", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            TextButton(onClick = onCancel, modifier = Modifier.padding(top = 8.dp)) {
                Text("Отмена", color = TrackMePurple.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun ProfileInputRow(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    errorText: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var isEnabled by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }

    LaunchedEffect(value) {
        if (textFieldValueState.text != value) {
            textFieldValueState = textFieldValueState.copy(text = value)
        }
    }

    LaunchedEffect(isEnabled) {
        if (isEnabled) {
            textFieldValueState = textFieldValueState.copy(
                selection = TextRange(textFieldValueState.text.length)
            )
            focusRequester.requestFocus()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clickable { isEnabled = true }, // <-- Добавлено сюда
                color    = TrackMePurpleLight.copy(alpha = 0.2f),
                shape    = RoundedCornerShape(50),
                border   = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isEnabled) (if (isError) Color.Red else TrackMePurple) else Color.Transparent
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    BasicTextField(
                        value         = textFieldValueState,
                        onValueChange = { newFieldValue ->
                            textFieldValueState = newFieldValue
                            onValueChange(newFieldValue.text)
                        },
                        enabled       = isEnabled,
                        singleLine    = true,
                        textStyle     = TextStyle(color = TrackMePurple, fontSize = 16.sp, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            isEnabled = false
                            focusManager.clearFocus()
                        }),
                        cursorBrush   = SolidColor(TrackMePurple),
                        modifier      = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .focusRequester(focusRequester)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick  = {
                    isEnabled = !isEnabled
                    if (!isEnabled) focusManager.clearFocus()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Edit, null, tint = if (isEnabled) Color.Gray else TrackMePurple)
            }
        }
        if (isError && isEnabled) {
            Text(errorText, color = Color.Red, fontSize = 12.sp,
                modifier = Modifier.padding(start = 20.dp, top = 2.dp))
        }
    }
}