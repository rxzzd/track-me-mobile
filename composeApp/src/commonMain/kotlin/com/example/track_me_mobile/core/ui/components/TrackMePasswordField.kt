package com.example.track_me_mobile.core.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import org.jetbrains.compose.resources.painterResource

// Импорты сгенерированных ресурсов
import com.example.track_me_mobile.generated.resources.Res
import com.example.track_me_mobile.generated.resources.invisible
import com.example.track_me_mobile.generated.resources.visible

import com.example.track_me_mobile.core.ui.theme.TrackMeDeepPurple

@Composable
fun TrackMePasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    // Состояние видимости пароля
    var passwordVisible by remember { mutableStateOf(false) }

    TrackMeTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        // Если passwordVisible == true, показываем текст, иначе - точки
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = painterResource(
                        if (passwordVisible) Res.drawable.visible else Res.drawable.invisible
                    ),
                    contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                    tint = TrackMeDeepPurple
                )
            }
        }
    )
}