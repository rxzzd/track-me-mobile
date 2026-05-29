package com.example.track_me_mobile.features.meetings.presentation

import androidx.compose.ui.graphics.Color
import com.example.track_me_mobile.core.ui.theme.TrackMePurple
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MeetingScreenUtilsTest {

    @Test
    fun `getTeamStatusUi maps backend statuses to Russian labels`() {
        assertEquals("Есть проблемы", getTeamStatusUi("WITH_ISSUES"))
        assertEquals("Есть большие проблемы", getTeamStatusUi("MANY_ISSUES"))
        assertEquals("Всё ок", getTeamStatusUi("OK"))
        assertEquals("Всё ок", getTeamStatusUi("ANY_OTHER_STATUS"))
    }

    @Test
    fun `getMeetingStatusUi maps backend statuses to Russian labels`() {
        assertEquals("Состоялась", getMeetingStatusUi("COMPLETED"))
        assertEquals("Не состоялась", getMeetingStatusUi("COMPLETED_AS_NOT_HAPPENED"))
        assertEquals("Не указана", getMeetingStatusUi("SCHEDULED"))
        assertEquals("Не указана", getMeetingStatusUi("UNKNOWN"))
    }

    @Test
    fun `formatMeetingDate returns formatted day and month or fallback on invalid input`() {
        assertEquals("05.08", formatMeetingDate("2024-08-05T00:00:00Z"))
        assertEquals("01.01", formatMeetingDate("not-a-date"))
    }

    @Test
    fun `isMeetingDatePassed returns true for old dates and false for future dates`() {
        assertTrue(isMeetingDatePassed("2020-01-01T00:00:00Z"))
        assertFalse(isMeetingDatePassed("2099-01-01T00:00:00Z"))
        assertFalse(isMeetingDatePassed("invalid-date"))
    }

    @Test
    fun `areMeetingFieldsFilled only returns true when all required fields are not blank`() {
        assertTrue(areMeetingFieldsFilled("next", "current", "https://link"))
        assertFalse(areMeetingFieldsFilled("", "current", "https://link"))
        assertFalse(areMeetingFieldsFilled("next", "", "https://link"))
        assertFalse(areMeetingFieldsFilled("next", "current", ""))
    }

    @Test
    fun `shouldShowBothStatusButtons returns true only when status is unspecified`() {
        assertTrue(shouldShowBothStatusButtons("Не указана"))
        assertFalse(shouldShowBothStatusButtons("Состоялась"))
        assertFalse(shouldShowBothStatusButtons("Не состоялась"))
    }

    @Test
    fun `getTeamStatusBadgeColor returns the correct color for each team status`() {
        assertEquals(Color(0xFF6DB371), getTeamStatusBadgeColor("Всё ок"))
        assertEquals(Color(0xFFE5D170), getTeamStatusBadgeColor("Есть проблемы"))
        assertEquals(Color(0xFFD36D6D), getTeamStatusBadgeColor("Есть большие проблемы"))
        assertEquals(TrackMePurple, getTeamStatusBadgeColor("Новая статус строка"))
    }

    @Test
    fun `hasValidScreenshotUri rejects invalid urls and accepts valid long urls`() {
        assertFalse(hasValidScreenshotUri(null))
        assertFalse(hasValidScreenshotUri(""))
        assertFalse(hasValidScreenshotUri("https://example.com/null"))
        assertFalse(hasValidScreenshotUri("https://example.com/short"))
        assertTrue(hasValidScreenshotUri("https://example.com/images/very-long-url-for-image.png?token=abc123"))
    }
}
