package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.runtime.*

/**
 * Основной компонент навигации для управления состоянием экранов Встречи.
 * Здесь хранятся все данные, которые редактируются пользователем.
 */
@Composable
fun MeetingNavigation(onBack: () -> Unit) {
    // Переключатель между экраном просмотра (false) и редактирования (true)
    var isEditing by remember { mutableStateOf(false) }

    // Состояния для хранения данных встречи
    var tasks by remember { mutableStateOf("Цель проекта — разработать MVP мобильного приложения для трекинга задач.") }
    var teamInfo by remember { mutableStateOf("Команда работает в штатном режиме. Все участники присутствовали на встрече.") }
    var status by remember { mutableStateOf("Всё ок") } // Статус команды (цветной индикатор)
    var linkRecord by remember { mutableStateOf("https://zoom.us/rec/play/xyz123") }
    var linkVideo by remember { mutableStateOf("https://meet.google.com/abc-defg-hij") }
    var date by remember { mutableStateOf("20.02") }

    // Новое состояние: Состоялась встреча или нет
    var meetingResult by remember { mutableStateOf("") } // Возможные значения: "", "Состоялась", "Не состоялась"

    if (isEditing) {
        // Экран редактирования
        MeetingEditScreen(
            initialTasks = tasks,
            initialTeamInfo = teamInfo,
            initialStatus = status,
            initialLinkRecord = linkRecord,
            initialLinkVideo = linkVideo,
            initialDate = date,
            initialMeetingResult = meetingResult,
            // Передаем обновленные данные обратно в навигацию
            onSave = { t, info, rec, vid, st, d, res ->
                tasks = t
                teamInfo = info
                linkRecord = rec
                linkVideo = vid
                status = st
                date = d
                meetingResult = res
                isEditing = false
            },
            onBack = { isEditing = false }
        )
    } else {
        // Экран просмотра деталей
        MeetingDetailScreen(
            tasks = tasks,
            teamInfo = teamInfo,
            status = status,
            linkRecord = linkRecord,
            linkVideo = linkVideo,
            date = date,
            meetingResult = meetingResult,
            onEditClick = { isEditing = true },
            onBack = onBack
        )
    }
}