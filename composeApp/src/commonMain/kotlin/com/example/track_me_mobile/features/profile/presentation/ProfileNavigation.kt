package com.example.track_me_mobile.features.profile.presentation

import androidx.compose.runtime.*

@Composable
fun ProfileNavigation() {
    // Состояние переключения между экранами
    var isEditing by remember { mutableStateOf(false) }

    // Основные данные пользователя (хранятся здесь постоянно)
    var name by remember { mutableStateOf("Иванов Иван Иванович") }
    var email by remember { mutableStateOf("ivan@mail.ru") }
    var phone by remember { mutableStateOf("+79999999999") }
    var telegram by remember { mutableStateOf("@IVANIVAN") }

    if (isEditing) {
        // ЭКРАН РЕДАКТИРОВАНИЯ
        ProfileEditScreen(
            initialName = name,
            initialEmail = email,
            initialPhone = phone,
            initialTelegram = telegram,
            onSaveComplete = { newName, newEmail, newPhone, newTelegram ->
                // Обновляем основные данные только при нажатии "Сохранить"
                name = newName
                email = newEmail
                phone = newPhone
                telegram = newTelegram
                isEditing = false
            },
            onCancel = {
                // Возвращаемся назад без изменения основных данных
                isEditing = false
            }
        )
    } else {
        // ЭКРАН ПРОСМОТРА
        ProfileScreen(
            name = name,
            email = email,
            phone = phone,
            telegram = telegram,
            onNavigateToEdit = {
                isEditing = true
            }
        )
    }
}