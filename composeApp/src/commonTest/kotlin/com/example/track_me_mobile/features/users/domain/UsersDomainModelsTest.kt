package com.example.track_me_mobile.features.users.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UsersDomainModelsTest {

    @Test
    fun `TrackerUser and AdminUser data classes`() {
        val tracker = TrackerUser("1", "Alice", "alice", true)
        val admin = AdminUser("2", "Bob Admin", "bob", false)

        assertEquals("Alice", tracker.fullName)
        assertEquals("alice", tracker.telegramNick)
        assertTrue(tracker.isConfirmed)

        assertEquals("Bob Admin", admin.fullName)
        assertFalse(admin.isConfirmed)
    }
}
