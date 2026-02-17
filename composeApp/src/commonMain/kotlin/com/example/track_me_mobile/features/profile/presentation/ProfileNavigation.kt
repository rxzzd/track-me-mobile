package com.example.track_me_mobile.features.profile.presentation

import androidx.compose.runtime.*

@Composable
fun ProfileNavigation() {
    var isEditing by remember { mutableStateOf(false) }

    // Хранилище данных
    var name by remember { mutableStateOf("Иванов Иван Иванович") }
    var email by remember { mutableStateOf("ivan@mail.ru") }
    var phone by remember { mutableStateOf("+79999999999") }
    var telegram by remember { mutableStateOf("@IVANIVAN") }

    if (isEditing) {
        ProfileEditScreen(
            initialName = name,
            initialEmail = email,
            initialPhone = phone,
            initialTelegram = telegram,
            onSaveComplete = { n, e, p, t ->
                name = n
                email = e
                phone = p
                telegram = t
                isEditing = false
            },
            onCancel = { isEditing = false }
        )
    } else {
        ProfileScreen(
            name = name,
            email = email,
            phone = phone,
            telegram = telegram,
            onNavigateToEdit = { isEditing = true }
        )
    }
}