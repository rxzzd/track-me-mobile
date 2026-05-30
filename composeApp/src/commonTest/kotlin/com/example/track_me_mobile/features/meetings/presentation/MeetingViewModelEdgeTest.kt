package com.example.track_me_mobile.features.meetings.presentation

import com.example.track_me_mobile.features.meetings.domain.MeetingRepository
import com.example.track_me_mobile.features.meetings.domain.models.Meeting
import com.example.track_me_mobile.features.meetings.domain.models.MeetingPage
import com.example.track_me_mobile.features.meetings.domain.models.MeetingUpdateRequest
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
import com.example.track_me_mobile.features.team_card.domain.models.TrackerUser
import com.example.track_me_mobile.features.teams.domain.models.NtiMarket
import com.example.track_me_mobile.features.teams.domain.models.Stream
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingViewModelEdgeTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== MeetingScreenUtils Edge Cases ====================

    @Test
    fun `getTeamStatusUi handles all known statuses`() {
        assertEquals("Есть проблемы", getTeamStatusUi("WITH_ISSUES"))
        assertEquals("Есть большие проблемы", getTeamStatusUi("MANY_ISSUES"))
        assertEquals("Всё ок", getTeamStatusUi("OK"))
        assertEquals("Всё ок", getTeamStatusUi(""))
        assertEquals("Всё ок", getTeamStatusUi("null"))
        assertEquals("Всё ок", getTeamStatusUi("SOME_RANDOM_STATUS"))
    }

    @Test
    fun `getMeetingStatusUi handles all known statuses`() {
        assertEquals("Состоялась", getMeetingStatusUi("COMPLETED"))
        assertEquals("Не состоялась", getMeetingStatusUi("COMPLETED_AS_NOT_HAPPENED"))
        assertEquals("Не указана", getMeetingStatusUi("SCHEDULED"))
        assertEquals("Не указана", getMeetingStatusUi(""))
        assertEquals("Не указана", getMeetingStatusUi("null"))
        assertEquals("Не указана", getMeetingStatusUi("PLANNED"))
    }

    @Test
    fun `formatMeetingDate handles various valid ISO formats`() {
        // With Z suffix
        assertEquals("05.08", formatMeetingDate("2024-08-05T00:00:00Z"))
        // With timezone offset
        assertEquals("05.08", formatMeetingDate("2024-08-05T00:00:00+07:00"))
        // With different time; in Asia/Tomsk this is already the next day.
        assertEquals("06.08", formatMeetingDate("2024-08-05T23:59:59Z"))
    }

    @Test
    fun `formatMeetingDate handles edge case dates`() {
        // Leap year
        assertEquals("29.02", formatMeetingDate("2024-02-29T12:00:00Z"))
        // First day of year
        assertEquals("01.01", formatMeetingDate("2025-01-01T00:00:00Z"))
        // Last day of year
        assertEquals("31.12", formatMeetingDate("2024-12-31T12:00:00Z"))
    }

    @Test
    fun `formatMeetingDate returns fallback for various invalid inputs`() {
        assertEquals("01.01", formatMeetingDate(""))
        assertEquals("01.01", formatMeetingDate("not-a-date"))
        assertEquals("01.01", formatMeetingDate("2024-13-01T00:00:00Z")) // invalid month
        assertEquals("01.01", formatMeetingDate("2024-00-01T00:00:00Z")) // zero month
        assertEquals("01.01", formatMeetingDate("abcd-ef-ghT00:00:00Z"))
    }

    @Test
    fun `isMeetingDatePassed handles various dates`() {
        // Past dates
        assertTrue(isMeetingDatePassed("2020-01-01T00:00:00Z"))
        assertTrue(isMeetingDatePassed("2023-12-31T23:59:59Z"))
        // Future dates
        assertFalse(isMeetingDatePassed("2099-01-01T00:00:00Z"))
        assertFalse(isMeetingDatePassed("2100-12-31T00:00:00Z"))
        // Invalid dates return false
        assertFalse(isMeetingDatePassed(""))
        assertFalse(isMeetingDatePassed("invalid"))
    }

    @Test
    fun `areMeetingFieldsFilled handles whitespace`() {
        assertTrue(areMeetingFieldsFilled("next", "current", "link"))
        assertFalse(areMeetingFieldsFilled("   ", "current", "link"))
        assertFalse(areMeetingFieldsFilled("next", "   ", "link"))
        assertFalse(areMeetingFieldsFilled("next", "current", "   "))
        assertFalse(areMeetingFieldsFilled("", "", ""))
        assertFalse(areMeetingFieldsFilled(" ", " ", " "))
    }

    @Test
    fun `shouldShowBothStatusButtons handles all cases`() {
        assertTrue(shouldShowBothStatusButtons("Не указана"))
        assertFalse(shouldShowBothStatusButtons("Состоялась"))
        assertFalse(shouldShowBothStatusButtons("Не состоялась"))
        assertFalse(shouldShowBothStatusButtons(""))
        assertFalse(shouldShowBothStatusButtons("Любой другой статус"))
    }

    @Test
    fun `getTeamStatusBadgeColor handles all known statuses`() {
        assertEquals(androidx.compose.ui.graphics.Color(0xFF6DB371), getTeamStatusBadgeColor("Всё ок"))
        assertEquals(androidx.compose.ui.graphics.Color(0xFFE5D170), getTeamStatusBadgeColor("Есть проблемы"))
        assertEquals(androidx.compose.ui.graphics.Color(0xFFD36D6D), getTeamStatusBadgeColor("Есть большие проблемы"))
        assertEquals(com.example.track_me_mobile.core.ui.theme.TrackMePurple, getTeamStatusBadgeColor(""))
        assertEquals(com.example.track_me_mobile.core.ui.theme.TrackMePurple, getTeamStatusBadgeColor("Неизвестный статус"))
    }

    @Test
    fun `getResultHintText handles all combinations`() {
        assertEquals("Заполните все поля встречи", getResultHintText(isDatePassed = true, allFieldsFilled = false))
        assertEquals("Результат можно отметить после даты встречи", getResultHintText(isDatePassed = false, allFieldsFilled = true))
        assertEquals("", getResultHintText(isDatePassed = true, allFieldsFilled = true))
        assertEquals("", getResultHintText(isDatePassed = false, allFieldsFilled = false))
    }

    @Test
    fun `isResultActionEnabled handles all combinations`() {
        assertTrue(isResultActionEnabled(isDatePassed = true, allFieldsFilled = true))
        assertFalse(isResultActionEnabled(isDatePassed = false, allFieldsFilled = true))
        assertFalse(isResultActionEnabled(isDatePassed = true, allFieldsFilled = false))
        assertFalse(isResultActionEnabled(isDatePassed = false, allFieldsFilled = false))
    }

    @Test
    fun `mapTeamStatusUiToApi handles all known labels`() {
        assertEquals("WITH_ISSUES", mapTeamStatusUiToApi("Есть проблемы"))
        assertEquals("MANY_ISSUES", mapTeamStatusUiToApi("Есть большие проблемы"))
        assertEquals("OK", mapTeamStatusUiToApi("Всё ок"))
        assertEquals("OK", mapTeamStatusUiToApi(""))
        assertEquals("OK", mapTeamStatusUiToApi("Неизвестный статус"))
    }

    @Test
    fun `mapResultStatusUiToApi handles all known labels`() {
        assertEquals("COMPLETED", mapResultStatusUiToApi("Состоялась"))
        assertEquals("COMPLETED_AS_NOT_HAPPENED", mapResultStatusUiToApi("Не состоялась"))
        assertEquals("SCHEDULED", mapResultStatusUiToApi("Не указана"))
        assertEquals("SCHEDULED", mapResultStatusUiToApi(""))
        assertEquals("SCHEDULED", mapResultStatusUiToApi("Любой другой"))
    }

    // ==================== MeetingViewModel updateIsoDateWithUi Edge Cases ====================

    @Test
    fun `MeetingViewModel updateIsoDateWithUi replaces month and day correctly`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Всё ок", "l", "15.3", "Состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("2024-03-15T10:00:00Z", repo.lastRequest?.startDate)
    }

    @Test
    fun `MeetingViewModel updateIsoDateWithUi handles single digit day and month`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Всё ок", "l", "1.1", "Состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("2024-01-01T10:00:00Z", repo.lastRequest?.startDate)
    }

    @Test
    fun `MeetingViewModel updateIsoDateWithUi handles double digit day and month`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Всё ок", "l", "31.12", "Состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("2024-12-31T10:00:00Z", repo.lastRequest?.startDate)
    }

    @Test
    fun `MeetingViewModel updateIsoDateWithUi preserves original date when uiDate is invalid`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Всё ок", "l", "invalid", "Состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("2024-08-05T10:00:00Z", repo.lastRequest?.startDate)
    }

    @Test
    fun `MeetingViewModel updateIsoDateWithUi preserves original date when uiDate is empty`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Всё ок", "l", "", "Состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("2024-08-05T10:00:00Z", repo.lastRequest?.startDate)
    }

    @Test
    fun `MeetingViewModel updateIsoDateWithUi handles single part date`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Всё ок", "l", "15", "Состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("2024-08-05T10:00:00Z", repo.lastRequest?.startDate)
    }

    @Test
    fun `MeetingViewModel updateIsoDateWithUi handles three part date`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Всё ок", "l", "15.3.2024", "Состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("2024-03-15T10:00:00Z", repo.lastRequest?.startDate)
    }

    @Test
    fun `MeetingViewModel updateMeeting maps Не состоялась correctly`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Всё ок", "l", "5.8", "Не состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("COMPLETED_AS_NOT_HAPPENED", repo.lastRequest?.status)
        assertEquals("OK", repo.lastRequest?.teamStatus)
    }

    @Test
    fun `MeetingViewModel updateMeeting maps Есть большие проблемы correctly`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Есть большие проблемы", "l", "5.8", "SCHEDULED")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("MANY_ISSUES", repo.lastRequest?.teamStatus)
        assertEquals("SCHEDULED", repo.lastRequest?.status)
    }

    @Test
    fun `MeetingViewModel updateMeeting passes tasks and link correctly`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "new-next", "new-current", "Всё ок", "https://new-link.com", "5.8", "Состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("new-next", repo.lastRequest?.tasksNextMeeting)
        assertEquals("new-current", repo.lastRequest?.tasksCurrentMeeting)
        assertEquals("https://new-link.com", repo.lastRequest?.link)
    }

    @Test
    fun `MeetingViewModel updateMeeting preserves meeting number`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "042", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Всё ок", "l", "5.8", "Состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("042", repo.lastRequest?.number)
    }

    @Test
    fun `MeetingViewModel updateMeeting preserves teamCardId`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "team-card-42", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "team-card-42", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "Всё ок", "l", "5.8", "Состоялась")
        advanceUntilIdle()

        assertNotNull(repo.lastRequest)
        assertEquals("team-card-42", repo.lastRequest?.teamCardId)
    }

    @Test
    fun `MeetingViewModel uploadImage with empty byte array`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.uploadImage(byteArrayOf())
        advanceUntilIdle()

        assertTrue(repo.uploadCalled)
    }

    @Test
    fun `MeetingViewModel uploadImage with large byte array`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        val largeArray = ByteArray(1024 * 1024) // 1MB
        vm.uploadImage(largeArray)
        advanceUntilIdle()

        assertTrue(repo.uploadCalled)
    }

    @Test
    fun `MeetingViewModel loadTeamCard with custom room link`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val teamCard = TeamCard(
            id = "t1", name = "Team", meetingRoomLink = "https://custom-room.example.com/abc",
            description = "desc", status = "ACTIVE", username = "u", enabled = true,
            ntiMarkets = emptyList(), readinessLevel = "1", averageGrade = null,
            stream = null, meetingsCount = 0, meetingsCompletedCount = 0, meetingsNotHappenedCount = 0
        )
        val vm = MeetingViewModel(FakeMeetingRepo(listOf(meeting)), "m1", "t1", FakeTeamCardRepo(teamCard))
        advanceUntilIdle()

        assertEquals("https://custom-room.example.com/abc", vm.meetingRoomLink)
    }

    @Test
    fun `MeetingViewModel loadTeamCard with null room link`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val teamCard = TeamCard(
            id = "t1", name = "Team", meetingRoomLink = "",
            description = "desc", status = "ACTIVE", username = "u", enabled = true,
            ntiMarkets = emptyList(), readinessLevel = "1", averageGrade = null,
            stream = null, meetingsCount = 0, meetingsCompletedCount = 0, meetingsNotHappenedCount = 0
        )
        val vm = MeetingViewModel(FakeMeetingRepo(listOf(meeting)), "m1", "t1", FakeTeamCardRepo(teamCard))
        advanceUntilIdle()

        assertEquals("", vm.meetingRoomLink)
    }

    @Test
    fun `MeetingViewModel isLoading is true during loading and false after`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        assertFalse(vm.isLoading) // Not yet advanced
        advanceUntilIdle()
        assertFalse(vm.isLoading) // Loading complete
    }

    @Test
    fun `MeetingViewModel errorMessage is null on successful load`() = runTest {
        val meeting = Meeting(
            id = "m1", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()
        assertNull(vm.errorMessage)
    }

    @Test
    fun `MeetingViewModel errorMessage set when meeting not found`() = runTest {
        val meeting = Meeting(
            id = "other-id", teamCardId = "t1", number = "001", link = "l",
            startDate = "2024-08-05T10:00:00Z", teamStatus = "OK", status = "SCHEDULED",
            tasksCurrentMeeting = "c", tasksNextMeeting = "n", imageUrl = null
        )
        val repo = FakeMeetingRepo(meetings = listOf(meeting))
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()
        assertNotNull(vm.errorMessage)
        assertTrue(vm.errorMessage!!.contains("Встреча не найдена"))
    }

    @Test
    fun `MeetingViewModel errorMessage set on repository failure`() = runTest {
        val repo = FailingMeetingRepo()
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()
        assertNotNull(vm.errorMessage)
    }

    @Test
    fun `MeetingViewModel updateMeeting with null meeting does nothing`() = runTest {
        val repo = FakeMeetingRepo(meetings = emptyList())
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.updateMeeting("m1", "n", "c", "OK", "l", "5.8", "SCHEDULED")
        advanceUntilIdle()

        assertNull(repo.lastRequest)
    }

    @Test
    fun `MeetingViewModel uploadImage with null meeting does nothing`() = runTest {
        val repo = FakeMeetingRepo(meetings = emptyList())
        val vm = MeetingViewModel(repo, "m1", "t1", FakeTeamCardRepo())
        advanceUntilIdle()

        vm.uploadImage(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertFalse(repo.uploadCalled)
    }

    // ==================== hasValidScreenshotUri Edge Cases ====================

    @Test
    fun `hasValidScreenshotUri rejects null`() {
        assertFalse(hasValidScreenshotUri(null))
    }

    @Test
    fun `hasValidScreenshotUri rejects empty string`() {
        assertFalse(hasValidScreenshotUri(""))
    }

    @Test
    fun `hasValidScreenshotUri rejects null-containing urls`() {
        assertFalse(hasValidScreenshotUri("https://example.com/null"))
        assertFalse(hasValidScreenshotUri("/null/image.png"))
        assertFalse(hasValidScreenshotUri("null"))
    }

    @Test
    fun `hasValidScreenshotUri rejects short urls`() {
        assertFalse(hasValidScreenshotUri("short"))
        assertFalse(hasValidScreenshotUri("https://example.com/x"))
    }

    @Test
    fun `hasValidScreenshotUri accepts long urls`() {
        assertTrue(hasValidScreenshotUri("https://example.com/images/very-long-url-for-image.png?token=abc123"))
        assertTrue(hasValidScreenshotUri("a".repeat(51)))
    }

    @Test
    fun `hasValidScreenshotUri boundary at 50 chars`() {
        assertFalse(hasValidScreenshotUri("a".repeat(50)))
        assertTrue(hasValidScreenshotUri("a".repeat(51)))
    }

    @Test
    fun `hasValidScreenshotUri handles urls with null in path but long enough`() {
        // Contains "/null" but length > 50
        assertFalse(hasValidScreenshotUri("https://example.com/null/image/very/long/url/for/test.png"))
    }

    @Test
    fun `getScreenshotPlaceholderText returns correct text for editing mode`() {
        assertEquals("Нажмите, чтобы загрузить", getScreenshotPlaceholderText(true))
    }

    @Test
    fun `getScreenshotPlaceholderText returns correct text for view mode`() {
        assertEquals("Прикрепить скриншот", getScreenshotPlaceholderText(false))
    }

    // ==================== Helper Classes ====================

    private class FakeMeetingRepo(
        private val meetings: List<Meeting>
    ) : MeetingRepository {
        var lastRequest: MeetingUpdateRequest? = null
        var uploadCalled = false

        override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> =
            Result.success(MeetingPage(meetings, 1, meetings.size.toLong(), 0))

        override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> {
            lastRequest = request
            return Result.success(Unit)
        }

        override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)

        override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> {
            uploadCalled = true
            return Result.success(Unit)
        }

        override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> = Result.success(Unit)
    }

    private class FailingMeetingRepo : MeetingRepository {
        override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> =
            Result.failure(RuntimeException("Network error"))

        override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> =
            Result.failure(RuntimeException("Update error"))

        override suspend fun deleteMeeting(meetingId: String): Result<Unit> =
            Result.failure(RuntimeException("Delete error"))

        override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> =
            Result.failure(RuntimeException("Upload error"))

        override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> =
            Result.failure(RuntimeException("Create error"))
    }

    private class FakeTeamCardRepo(
        private val teamCard: TeamCard = TeamCard(
            id = "t1", name = "Team", meetingRoomLink = "room-link",
            description = "desc", status = "ACTIVE", username = "u", enabled = true,
            ntiMarkets = emptyList(), readinessLevel = "1", averageGrade = null,
            stream = null, meetingsCount = 0, meetingsCompletedCount = 0, meetingsNotHappenedCount = 0
        )
    ) : TeamCardRepository {
        override suspend fun getTeamById(id: String): Result<TeamCard> = Result.success(teamCard)
        override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("tracker")
        override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
        override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
        override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
        override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
        override suspend fun updateTeam(request: com.example.track_me_mobile.features.team_card.domain.models.UpdateTeamRequest): Result<Unit> = Result.success(Unit)
        override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
    }
}
