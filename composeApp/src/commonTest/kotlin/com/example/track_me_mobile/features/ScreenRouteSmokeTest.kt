package com.example.track_me_mobile.features

import cafe.adriel.voyager.core.screen.Screen
import com.example.track_me_mobile.features.meetings.presentation.MeetingScreen
import com.example.track_me_mobile.features.profile.presentation.ProfileEditScreen
import com.example.track_me_mobile.features.profile.presentation.ProfileScreen
import com.example.track_me_mobile.features.reports.presentation.ReportsListScreen
import com.example.track_me_mobile.features.reports.presentation.StreamMeetingReportScreen
import com.example.track_me_mobile.features.streams.presentation.AddStreamScreen
import com.example.track_me_mobile.features.streams.presentation.EditStreamScreen
import com.example.track_me_mobile.features.streams.presentation.StreamListScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScreenRouteSmokeTest {

    @Test
    fun `presentation route classes can be created as voyager screens`() {
        val screens = listOf(
            ProfileScreen(),
            ProfileEditScreen(),
            ReportsListScreen(),
            StreamMeetingReportScreen("stream-1"),
            AddStreamScreen(),
            EditStreamScreen("stream-2"),
            StreamListScreen(),
            MeetingScreen("meeting-1", "team-1")
        )

        assertEquals(8, screens.size)
        assertTrue(screens.all { it is Screen })
        assertEquals("stream-1", privateField(screens[3], "streamId"))
        assertEquals("stream-2", privateField(screens[5], "streamId"))
        assertEquals("meeting-1", privateField(screens[7], "meetingId"))
        assertEquals("team-1", privateField(screens[7], "teamCardId"))
    }

    @Test
    fun `stream meeting report date formatter handles normal and bad values`() {
        val formatter = Class
            .forName("com.example.track_me_mobile.features.reports.presentation.StreamMeetingReportScreenKt")
            .getDeclaredMethod("formatReportDate", String::class.java)
        formatter.isAccessible = true

        assertEquals("01.01.2024", formatter.invoke(null, "2024-01-01T00:00:00Z"))
        assertEquals("bad-date", formatter.invoke(null, "bad-date"))
        assertTrue((formatter.invoke(null, null) as String).isNotEmpty())
        assertTrue((formatter.invoke(null, "") as String).isNotEmpty())
    }

    private fun privateField(target: Any, name: String): String {
        val field = target::class.java.getDeclaredField(name)
        field.isAccessible = true
        return field.get(target) as String
    }
}
