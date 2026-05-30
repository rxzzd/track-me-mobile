package com.example.track_me_mobile.features.team_card.presentation

import com.example.track_me_mobile.features.meetings.domain.MeetingRepository
import com.example.track_me_mobile.features.meetings.domain.models.Meeting
import com.example.track_me_mobile.features.meetings.domain.models.MeetingPage
import com.example.track_me_mobile.features.meetings.domain.models.MeetingUpdateRequest
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TeamMeetingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== init / loadMeetings ====================

    @Test
    fun `init loads meetings and sets Success state`() = runTest {
        val meetings = listOf(
            sampleMeeting("m1", "001", "2024-08-05T10:00:00Z", "SCHEDULED"),
            sampleMeeting("m2", "002", "2024-08-06T10:00:00Z", "COMPLETED")
        )
        val repository = FakeMeetingRepository(meetings)
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        val state = viewModel.state
        assertIs<TeamMeetingsState.Success>(state)
        assertEquals(2, state.meetings.size)
    }

    @Test
    fun `loadMeetings sorts by number descending`() = runTest {
        val meetings = listOf(
            sampleMeeting("m1", "002", "2024-08-05T10:00:00Z", "SCHEDULED"),
            sampleMeeting("m2", "001", "2024-08-06T10:00:00Z", "COMPLETED"),
            sampleMeeting("m3", "003", "2024-08-07T10:00:00Z", "SCHEDULED")
        )
        val repository = FakeMeetingRepository(meetings)
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        val state = viewModel.state as TeamMeetingsState.Success
        assertEquals("003", state.meetings[0].number)
        assertEquals("002", state.meetings[1].number)
        assertEquals("001", state.meetings[2].number)
    }

    @Test
    fun `loadMeetings failure sets Error state`() = runTest {
        val repository = object : MeetingRepository {
            override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> =
                Result.failure(Exception("Network error"))
            override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)
            override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> = Result.success(Unit)
            override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        assertIs<TeamMeetingsState.Error>(viewModel.state)
    }

    @Test
    fun `loadMeetings with empty list returns Success with empty meetings`() = runTest {
        val repository = FakeMeetingRepository(emptyList())
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        val state = viewModel.state as TeamMeetingsState.Success
        assertTrue(state.meetings.isEmpty())
    }

    @Test
    fun `loadMeetings maps Meeting to MeetingItemUI correctly`() = runTest {
        val meeting = Meeting(
            id = "m1",
            teamCardId = "team-1",
            number = "005",
            link = "https://link",
            startDate = "2024-12-25T15:30:00Z",
            teamStatus = "OK",
            status = "COMPLETED",
            tasksCurrentMeeting = "current tasks",
            tasksNextMeeting = "next tasks",
            imageUrl = null
        )
        val repository = FakeMeetingRepository(listOf(meeting))
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        val state = viewModel.state as TeamMeetingsState.Success
        val item = state.meetings.first()
        assertEquals("m1", item.id)
        assertEquals("team-1", item.teamCardId)
        assertEquals("005", item.number)
        assertEquals("https://link", item.link)
        assertEquals("OK", item.teamStatus)
        assertEquals("COMPLETED", item.status)
        assertEquals("current tasks", item.tasksCurrentMeeting)
        assertEquals("next tasks", item.tasksNextMeeting)
        assertEquals("2024-12-25T15:30:00Z", item.startDateIso)
        assertEquals("Встреча №005", item.title)
    }



    // ==================== planNewMeeting ====================

    @Test
    fun `planNewMeeting creates meeting with next number and reloads`() = runTest {
        val meetings = listOf(
            sampleMeeting("m1", "003", "2024-08-05T10:00:00Z", "SCHEDULED"),
            sampleMeeting("m2", "001", "2024-08-06T10:00:00Z", "COMPLETED")
        )
        val repository = FakeMeetingRepository(meetings)
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        var successCalled = false
        viewModel.planNewMeeting("2024-09-01T10:00:00Z") { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals("4", repository.lastCreatedNumber) // (3 + 1).toString() = "4"
        assertEquals("team-1", repository.lastCreatedTeamCardId)
        assertEquals("2024-09-01T10:00:00Z", repository.lastCreatedStartDate)
    }

    @Test
    fun `planNewMeeting when state is not Success does nothing`() = runTest {
        val repository = object : MeetingRepository {
            override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> =
                Result.failure(Exception("fail"))
            override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)
            override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> = Result.success(Unit)
            override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        var successCalled = false
        viewModel.planNewMeeting("2024-09-01T10:00:00Z") { successCalled = true }
        advanceUntilIdle()

        assertFalse(successCalled)
    }

    @Test
    fun `planNewMeeting with empty meetings creates meeting number 1`() = runTest {
        val repository = FakeMeetingRepository(emptyList())
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        viewModel.planNewMeeting("2024-09-01T10:00:00Z") {}
        advanceUntilIdle()

        assertEquals("1", repository.lastCreatedNumber)
    }

    @Test
    fun `planNewMeeting isCreating is false after completion`() = runTest {
        val repository = FakeMeetingRepository(listOf(sampleMeeting("m1", "001", "2024-08-05T10:00:00Z", "SCHEDULED")))
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        viewModel.planNewMeeting("2024-09-01T10:00:00Z") {}
        advanceUntilIdle()

        assertFalse(viewModel.isCreating)
    }

    @Test
    fun `planNewMeeting with non-numeric numbers handles gracefully`() = runTest {
        val meetings = listOf(
            sampleMeeting("m1", "abc", "2024-08-05T10:00:00Z", "SCHEDULED")
        )
        val repository = FakeMeetingRepository(meetings)
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        viewModel.planNewMeeting("2024-09-01T10:00:00Z") {}
        advanceUntilIdle()

        // maxOrNull returns null, so nextNumber = 1
        assertEquals("1", repository.lastCreatedNumber)
    }

    // ==================== deleteMeeting ====================

    @Test
    fun `deleteMeeting calls repository and reloads`() = runTest {
        val meetings = listOf(
            sampleMeeting("m1", "001", "2024-08-05T10:00:00Z", "SCHEDULED"),
            sampleMeeting("m2", "002", "2024-08-06T10:00:00Z", "COMPLETED")
        )
        val repository = FakeMeetingRepository(meetings)
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        var successCalled = false
        viewModel.deleteMeeting("m1") { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals("m1", repository.lastDeletedMeetingId)
    }

    @Test
    fun `deleteMeeting reloads meetings after success`() = runTest {
        val meetings = mutableListOf(
            sampleMeeting("m1", "001", "2024-08-05T10:00:00Z", "SCHEDULED"),
            sampleMeeting("m2", "002", "2024-08-06T10:00:00Z", "COMPLETED")
        )
        val repository = object : MeetingRepository {
            var deleted = false
            override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> {
                val current = if (deleted) meetings.filter { it.id != "m1" } else meetings
                return Result.success(MeetingPage(content = current, totalPages = 1, totalElements = current.size.toLong(), currentPage = 0))
            }
            override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteMeeting(meetingId: String): Result<Unit> {
                deleted = true
                return Result.success(Unit)
            }
            override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> = Result.success(Unit)
            override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()
        assertEquals(2, (viewModel.state as TeamMeetingsState.Success).meetings.size)

        viewModel.deleteMeeting("m1") {}
        advanceUntilIdle()

        assertEquals(1, (viewModel.state as TeamMeetingsState.Success).meetings.size)
    }

    // ==================== updateMeetingDate ====================

    @Test
    fun `updateMeetingDate updates date and reloads`() = runTest {
        val meeting = sampleMeeting("m1", "001", "2024-08-05T10:00:00Z", "SCHEDULED")
        val repository = FakeMeetingRepository(listOf(meeting))
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        val item = (viewModel.state as TeamMeetingsState.Success).meetings.first()
        // 2024-09-01 in millis
        val newDateMillis = kotlinx.datetime.Instant.parse("2024-09-01T10:00:00Z").toEpochMilliseconds()

        var successCalled = false
        viewModel.updateMeetingDate(item, newDateMillis) { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertNotNull(repository.lastUpdateRequest)
        // The new date should contain 2024-09-01
        assertTrue(repository.lastUpdateRequest!!.startDate.contains("2024-09-01"))
    }

    @Test
    fun `updateMeetingDate uses safe defaults for blank fields`() = runTest {
        val meeting = Meeting(
            id = "m1",
            teamCardId = "team-1",
            number = "001",
            link = "",
            startDate = "2024-08-05T10:00:00Z",
            teamStatus = "OK",
            status = "SCHEDULED",
            tasksCurrentMeeting = "",
            tasksNextMeeting = "",
            imageUrl = null
        )
        val repository = FakeMeetingRepository(listOf(meeting))
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        val item = (viewModel.state as TeamMeetingsState.Success).meetings.first()
        val newDateMillis = kotlinx.datetime.Instant.parse("2024-09-01T10:00:00Z").toEpochMilliseconds()

        viewModel.updateMeetingDate(item, newDateMillis) {}
        advanceUntilIdle()

        // Safe defaults should be used for blank fields
        assertEquals("https://example.com", repository.lastUpdateRequest?.link)
    }

    // ==================== Meeting.toUI() edge cases ====================

    @Test
    fun `toUI maps all fields correctly for completed meeting`() = runTest {
        val meeting = Meeting(
            id = "m-completed",
            teamCardId = "team-1",
            number = "010",
            link = "https://meeting-link",
            startDate = "2024-11-15T14:00:00Z",
            teamStatus = "WITH_ISSUES",
            status = "COMPLETED",
            tasksCurrentMeeting = "Reviewed progress",
            tasksNextMeeting = "Prepare report",
            imageUrl = "https://image.url"
        )
        val repository = FakeMeetingRepository(listOf(meeting))
        val viewModel = TeamMeetingsViewModel("team-1", repository)

        advanceUntilIdle()

        val item = (viewModel.state as TeamMeetingsState.Success).meetings.first()
        assertEquals("m-completed", item.id)
        assertEquals("010", item.number)
        assertEquals("https://meeting-link", item.link)
        assertEquals("WITH_ISSUES", item.teamStatus)
        assertEquals("COMPLETED", item.status)
        assertEquals("Reviewed progress", item.tasksCurrentMeeting)
        assertEquals("Prepare report", item.tasksNextMeeting)
        assertEquals("2024-11-15T14:00:00Z", item.startDateIso)
        assertEquals("Встреча №010", item.title)
    }

    // ==================== Helpers ====================

    private fun sampleMeeting(
        id: String,
        number: String,
        startDate: String,
        status: String
    ) = Meeting(
        id = id,
        teamCardId = "team-1",
        number = number,
        link = "https://link",
        startDate = startDate,
        teamStatus = "OK",
        status = status,
        tasksCurrentMeeting = "tasks",
        tasksNextMeeting = "next",
        imageUrl = null
    )

    private class FakeMeetingRepository(
        private val meetings: List<Meeting>
    ) : MeetingRepository {
        var lastCreatedTeamCardId: String? = null
        var lastCreatedStartDate: String? = null
        var lastCreatedNumber: String? = null
        var lastDeletedMeetingId: String? = null
        var lastUpdateRequest: MeetingUpdateRequest? = null

        override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> {
            return Result.success(MeetingPage(content = meetings, totalPages = 1, totalElements = meetings.size.toLong(), currentPage = 0))
        }

        override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> {
            lastUpdateRequest = request
            return Result.success(Unit)
        }

        override suspend fun deleteMeeting(meetingId: String): Result<Unit> {
            lastDeletedMeetingId = meetingId
            return Result.success(Unit)
        }

        override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> = Result.success(Unit)

        override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> {
            lastCreatedTeamCardId = teamCardId
            lastCreatedStartDate = startDateIso
            lastCreatedNumber = number
            return Result.success(Unit)
        }
    }
}