package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.ui.graphics.Color
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun getTeamStatusUi(apiStatus: String): String = when (apiStatus) {
    "WITH_ISSUES" -> "Есть проблемы"
    "MANY_ISSUES" -> "Есть большие проблемы"
    "OK" -> "Всё ок"
    else -> "Всё ок"
}

fun getMeetingStatusUi(apiStatus: String): String = when (apiStatus) {
    "COMPLETED" -> "Состоялась"
    "COMPLETED_AS_NOT_HAPPENED" -> "Не состоялась"
    "SCHEDULED" -> "Не указана"
    else -> "Не указана"
}

fun formatMeetingDate(isoDate: String): String = try {
    val instant = Instant.parse(isoDate)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    "${date.dayOfMonth.toString().padStart(2, '0')}.${date.monthNumber.toString().padStart(2, '0')}"
} catch (_: Exception) {
    "01.01"
}

fun isMeetingDatePassed(isoDate: String): Boolean = try {
    val instant = Instant.parse(isoDate)
    instant < Clock.System.now()
} catch (_: Exception) {
    false
}

fun areMeetingFieldsFilled(tasksNext: String, tasksCurrent: String, link: String): Boolean =
    tasksNext.isNotBlank() && tasksCurrent.isNotBlank() && link.isNotBlank()

fun shouldShowBothStatusButtons(statusUi: String): Boolean = statusUi == "Не указана"

fun getTeamStatusBadgeColor(statusUi: String): Color = when (statusUi) {
    "Всё ок" -> Color(0xFF6DB371)
    "Есть проблемы" -> Color(0xFFE5D170)
    "Есть большие проблемы" -> Color(0xFFD36D6D)
    else -> TrackMePurple
}

fun getResultHintText(isDatePassed: Boolean, allFieldsFilled: Boolean): String = when {
    isDatePassed && !allFieldsFilled -> "Заполните все поля встречи"
    !isDatePassed && allFieldsFilled -> "Результат можно отметить после даты встречи"
    else -> ""
}

fun isResultActionEnabled(isDatePassed: Boolean, allFieldsFilled: Boolean): Boolean =
    isDatePassed && allFieldsFilled

fun mapTeamStatusUiToApi(statusUi: String): String = when (statusUi) {
    "Есть проблемы" -> "WITH_ISSUES"
    "Есть большие проблемы" -> "MANY_ISSUES"
    "Всё ок" -> "OK"
    else -> "OK"
}

fun mapResultStatusUiToApi(statusUi: String): String = when (statusUi) {
    "Состоялась" -> "COMPLETED"
    "Не состоялась" -> "COMPLETED_AS_NOT_HAPPENED"
    "Не указана" -> "SCHEDULED"
    else -> "SCHEDULED"
}
