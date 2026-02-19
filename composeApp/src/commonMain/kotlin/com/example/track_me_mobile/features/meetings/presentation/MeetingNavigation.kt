package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.runtime.*

@Composable
fun MeetingNavigation(onBack: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }

    var tasks by remember { mutableStateOf("Цель проекта — разработать MVP...") }
    var teamInfo by remember { mutableStateOf("Все участники присутствовали.") }
    var status by remember { mutableStateOf("Всё ок") }
    var linkRecord by remember { mutableStateOf("") }
    var linkVideo by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("20.02") }
    var meetingResult by remember { mutableStateOf("") }
    // 1. Добавляем состояние для скриншота
    var screenshotUri by remember { mutableStateOf<String?>(null) }

    if (isEditing) {
        MeetingEditScreen(
            initialTasks = tasks,
            initialTeamInfo = teamInfo,
            initialStatus = status,
            initialLinkRecord = linkRecord,
            initialLinkVideo = linkVideo,
            initialDate = date,
            initialMeetingResult = meetingResult,
            initialScreenshot = screenshotUri, // 2. Передаем скриншот
            onSave = { t, info, rec, vid, st, d, res, pic -> // 3. Принимаем 8 параметров
                tasks = t
                teamInfo = info
                linkRecord = rec
                linkVideo = vid
                status = st
                date = d
                meetingResult = res
                screenshotUri = pic // 4. Сохраняем скриншот
                isEditing = false
            },
            onBack = { isEditing = false }
        )
    } else {
        MeetingDetailScreen(
            tasks = tasks,
            teamInfo = teamInfo,
            status = status,
            linkRecord = linkRecord,
            linkVideo = linkVideo,
            date = date,
            meetingResult = meetingResult,
            screenshotUri = screenshotUri, // 5. Передаем в экран просмотра
            onEditClick = { isEditing = true },
            onBack = onBack
        )
    }
}