package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.runtime.*

@Composable
fun MeetingNavigation(onBack: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }

    var tasks by remember { mutableStateOf("Цель проекта — разработать MVP...") }
    var status by remember { mutableStateOf("Всё ок") }
    var link by remember { mutableStateOf("https://zoom.us/j/123") }
    var date by remember { mutableStateOf("25.04") }

    if (isEditing) {
        MeetingEditScreen(
            initialTasks = tasks,
            initialStatus = status,
            initialLink = link,
            initialDate = date,
            onSave = { updatedTasks, updatedLink, updatedStatus, updatedDate ->
                tasks = updatedTasks
                link = updatedLink
                status = updatedStatus
                date = updatedDate
                isEditing = false
            },
            onBack = { isEditing = false }
        )
    } else {
        MeetingDetailScreen(
            tasks = tasks,
            status = status,
            link = link,
            date = date,
            onEditClick = { isEditing = true },
            onBack = onBack
        )
    }
}