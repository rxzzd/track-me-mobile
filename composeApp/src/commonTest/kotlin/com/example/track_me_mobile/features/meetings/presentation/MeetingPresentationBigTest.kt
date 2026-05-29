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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingPresentationBigTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `formatMeetingDate returns fallback when passed invalid ISO string`() {
        assertEquals("01.01", formatMeetingDate("not-a-date"))
        assertEquals("05.08", formatMeetingDate("2024-08-05T00:00:00Z"))
    }

    @Test
    fun `getResultHintText returns correct hint text for missing fields and future date`() {
        assertEquals("Заполните все поля встречи", getResultHintText(isDatePassed = true, allFieldsFilled = false))
        assertEquals("Результат можно отметить после даты встречи", getResultHintText(isDatePassed = false, allFieldsFilled = true))
        assertEquals("", getResultHintText(isDatePassed = true, allFieldsFilled = true))
    }

    @Test
    fun `isResultActionEnabled returns true only when date passed and fields filled`() {
        assertTrue(isResultActionEnabled(isDatePassed = true, allFieldsFilled = true))
        assertFalse(isResultActionEnabled(isDatePassed = false, allFieldsFilled = true))
        assertFalse(isResultActionEnabled(isDatePassed = true, allFieldsFilled = false))
    }

    @Test
    fun `mapTeamStatusUiToApi maps russian team status labels to api codes`() {
        assertEquals("WITH_ISSUES", mapTeamStatusUiToApi("Есть проблемы"))
        assertEquals("MANY_ISSUES", mapTeamStatusUiToApi("Есть большие проблемы"))
        assertEquals("OK", mapTeamStatusUiToApi("Всё ок"))
        assertEquals("OK", mapTeamStatusUiToApi("Любой другой статус"))
    }

    @Test
    fun `mapResultStatusUiToApi maps russian meeting status labels to api codes`() {
        assertEquals("COMPLETED", mapResultStatusUiToApi("Состоялась"))
        assertEquals("COMPLETED_AS_NOT_HAPPENED", mapResultStatusUiToApi("Не состоялась"))
        assertEquals("SCHEDULED", mapResultStatusUiToApi("Не указана"))
        assertEquals("SCHEDULED", mapResultStatusUiToApi("Любой другой"))
    }

    @Test
    fun `getScreenshotPlaceholderText returns correct text for view and edit modes`() {
        assertEquals("Нажмите, чтобы загрузить", getScreenshotPlaceholderText(true))
        assertEquals("Прикрепить скриншот", getScreenshotPlaceholderText(false))
    }

    @Test
    fun `MeetingViewModel init loads meeting and team card successfully`() = runTest {
        val meeting = Meeting(
            id = "meeting-1",
            teamCardId = "team-1",
            number = "001",
            link = "https://meeting",
            startDate = "2024-08-05T10:00:00Z",
            teamStatus = "OK",
            status = "SCHEDULED",
            tasksCurrentMeeting = "current",
            tasksNextMeeting = "next",
            imageUrl = null
        )
        val repository = FakeMeetingRepository(meetings = listOf(meeting))
        val teamCardRepository = FakeTeamCardRepository(teamCard = sampleTeamCard())

        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", teamCardRepository)
        advanceUntilIdle()

        assertNotNull(viewModel.meeting)
        assertEquals("meeting-1", viewModel.meeting?.id)
        assertEquals("room-link", viewModel.meetingRoomLink)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `MeetingViewModel init sets error message when meeting not found`() = runTest {
        val repository = FakeMeetingRepository(meetings = emptyList())
        val teamCardRepository = FakeTeamCardRepository(teamCard = sampleTeamCard())

        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", teamCardRepository)
        advanceUntilIdle()

        assertNull(viewModel.meeting)
        assertEquals("Встреча не найдена (ID: meeting-1)", viewModel.errorMessage)
        assertEquals("room-link", viewModel.meetingRoomLink)
    }

    @Test
    fun `MeetingViewModel updateMeeting sends mapped api request and preserves padded date`() = runTest {
        val meeting = Meeting(
            id = "meeting-1",
            teamCardId = "team-1",
            number = "001",
            link = "https://meeting",
            startDate = "2024-08-05T10:00:00Z",
            teamStatus = "OK",
            status = "SCHEDULED",
            tasksCurrentMeeting = "current",
            tasksNextMeeting = "next",
            imageUrl = null
        )
        val repository = FakeMeetingRepository(meetings = listOf(meeting))
        val teamCardRepository = FakeTeamCardRepository(teamCard = sampleTeamCard())

        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", teamCardRepository)
        advanceUntilIdle()

        viewModel.updateMeeting(
            meetingId = "meeting-1",
            tasksNext = "next tasks",
            tasksCurrent = "current tasks",
            teamStatusUI = "Есть большие проблемы",
            link = "https://updated",
            uiDate = "5.9",
            meetingStatusUI = "Не состоялась"
        )
        advanceUntilIdle()

        assertEquals("https://updated", repository.lastUpdateRequest?.link)
        assertEquals("COMPLETED_AS_NOT_HAPPENED", repository.lastUpdateRequest?.status)
        assertEquals("MANY_ISSUES", repository.lastUpdateRequest?.teamStatus)
        assertEquals("2024-09-05T10:00:00Z", repository.lastUpdateRequest?.startDate)
    }

    @Test
    fun `MeetingViewModel uploadImage reloads meeting after successful upload`() = runTest {
        val meeting = Meeting(
            id = "meeting-1",
            teamCardId = "team-1",
            number = "001",
            link = "https://meeting",
            startDate = "2024-08-05T10:00:00Z",
            teamStatus = "OK",
            status = "SCHEDULED",
            tasksCurrentMeeting = "current",
            tasksNextMeeting = "next",
            imageUrl = "https://old-image"
        )
        val repository = FakeMeetingRepository(meetings = listOf(meeting))
        val teamCardRepository = FakeTeamCardRepository(teamCard = sampleTeamCard())

        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", teamCardRepository)
        advanceUntilIdle()

        viewModel.uploadImage(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertTrue(repository.uploadCalled)
        assertEquals("meeting-1", repository.lastUploadMeetingId)
        assertNotNull(viewModel.meeting)
    }

    @Test
    fun `MeetingViewModel loadMeetings sets errorMessage when repository fails`() = runTest {
        val repository = FailingMeetingRepository()
        val teamCardRepository = FakeTeamCardRepository(teamCard = sampleTeamCard())

        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", teamCardRepository)
        advanceUntilIdle()

        assertNull(viewModel.meeting)
        assertEquals("Repository failed", viewModel.errorMessage)
    }

    private fun sampleTeamCard() = TeamCard(
        id = "team-1",
        name = "Team 1",
        meetingRoomLink = "room-link",
        description = "desc",
        status = "ACTIVE",
        username = "team1",
        enabled = true,
        ntiMarkets = emptyList(),
        readinessLevel = "1",
        averageGrade = 3.0,
        stream = null,
        meetingsCount = 1,
        meetingsCompletedCount = 1,
        meetingsNotHappenedCount = 0
    )

    private class FakeMeetingRepository(
        private val meetings: List<Meeting>
    ) : MeetingRepository {
        var lastUpdateRequest: MeetingUpdateRequest? = null
        var uploadCalled = false
        var lastUploadMeetingId: String? = null

        override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> {
            return Result.success(
                MeetingPage(
                    content = meetings,
                    totalPages = 1,
                    totalElements = meetings.size.toLong(),
                    currentPage = 0
                )
            )
        }

        override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> {
            lastUpdateRequest = request
            return Result.success(Unit)
        }

        override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)

        override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> {
            uploadCalled = true
            lastUploadMeetingId = meetingId
            return Result.success(Unit)
        }

        override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeTeamCardRepository(
        private val teamCard: TeamCard
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

    private class FailingMeetingRepository : MeetingRepository {
        override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> = Result.failure(RuntimeException("Repository failed"))
        override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> = Result.failure(RuntimeException("Update failed"))
        override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.failure(RuntimeException("Delete failed"))
        override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> = Result.failure(RuntimeException("Upload failed"))
        override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> = Result.failure(RuntimeException("Create failed"))
    }
}
