package com.example.track_me_mobile.features.meetings.presentation

import com.example.track_me_mobile.features.meetings.domain.MeetingRepository
import com.example.track_me_mobile.features.meetings.domain.models.Meeting
import com.example.track_me_mobile.features.meetings.domain.models.MeetingPage
import com.example.track_me_mobile.features.meetings.domain.models.MeetingUpdateRequest
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
import com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest
import com.example.track_me_mobile.features.team_card.domain.models.TrackerUser
import com.example.track_me_mobile.features.team_card.domain.models.UpdateTeamRequest
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
class MeetingViewModelBigTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleMeeting(
        id: String = "meeting-1",
        teamCardId: String = "team-1",
        number: String = "001",
        link: String = "https://meeting",
        startDate: String = "2024-08-05T10:00:00",
        teamStatus: String = "OK",
        status: String = "SCHEDULED",
        tasksCurrent: String = "current",
        tasksNext: String = "next",
        imageUrl: String? = null
    ) = Meeting(id, teamCardId, number, link, startDate, teamStatus, status, tasksCurrent, tasksNext, imageUrl)

    private fun sampleTeamCard(
        id: String = "team-1",
        name: String = "Team 1",
        meetingRoomLink: String = "room-link"
    ) = TeamCard(
        id = id, name = name, meetingRoomLink = meetingRoomLink, description = "desc",
        status = "ACTIVE", username = "team1", enabled = true,
        ntiMarkets = emptyList(), readinessLevel = "1", averageGrade = 3.0,
        stream = null, meetingsCount = 1, meetingsCompletedCount = 1, meetingsNotHappenedCount = 0
    )

    // ========== MeetingViewModel init tests ==========

    @Test
    fun `init sets errorMessage when meeting not found in loaded list`() = runTest {
        val repository = FakeMeetingRepositoryBig(
            meetings = listOf(sampleMeeting(id = "other-id"))
        )
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        assertNull(viewModel.meeting)
        assertNotNull(viewModel.errorMessage)
        assertTrue(viewModel.errorMessage!!.contains("meeting-1"))
    }

    @Test
    fun `init sets errorMessage when getMeetings fails`() = runTest {
        val repository = object : MeetingRepository {
            override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> =
                Result.failure(Exception("Network error"))
            override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> =
                Result.success(Unit)
            override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)
            override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> = Result.success(Unit)
            override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> =
                Result.success(Unit)
        }
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        assertNull(viewModel.meeting)
        assertEquals("Network error", viewModel.errorMessage)
    }

    @Test
    fun `init sets meetingRoomLink from teamCardRepository`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val teamCardRepo = FakeTeamCardRepositoryBig(sampleTeamCard(meetingRoomLink = "custom-room"))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", teamCardRepo)
        advanceUntilIdle()

        assertEquals("custom-room", viewModel.meetingRoomLink)
    }

    @Test
    fun `init does not set meetingRoomLink when getTeamById fails`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val teamCardRepo = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.failure(Exception("fail"))
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun createTeam(request: CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", teamCardRepo)
        advanceUntilIdle()

        assertNull(viewModel.meetingRoomLink)
    }

    // ========== updateMeeting tests ==========

    @Test
    fun `updateMeeting does nothing when meeting is null`() = runTest {
        val repository = FakeMeetingRepositoryBig(meetings = listOf(sampleMeeting(id = "other")))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "tasks", "tasks", "Всё ок", "link", "01.01", "Состоялась")
        advanceUntilIdle()

        assertNull(repository.lastUpdateRequest)
    }

    @Test
    fun `updateMeeting maps teamStatusUI Ek problems to WITH_ISSUES`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Есть проблемы", "link", "05.08", "Состоялась")
        advanceUntilIdle()

        assertEquals("WITH_ISSUES", repository.lastUpdateRequest?.teamStatus)
    }

    @Test
    fun `updateMeeting maps teamStatusUI big problems to MANY_ISSUES`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Есть большие проблемы", "link", "05.08", "Состоялась")
        advanceUntilIdle()

        assertEquals("MANY_ISSUES", repository.lastUpdateRequest?.teamStatus)
    }

    @Test
    fun `updateMeeting maps teamStatusUI ok to OK`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "05.08", "Состоялась")
        advanceUntilIdle()

        assertEquals("OK", repository.lastUpdateRequest?.teamStatus)
    }

    @Test
    fun `updateMeeting maps unknown teamStatusUI to OK`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Неизвестно", "link", "05.08", "Состоялась")
        advanceUntilIdle()

        assertEquals("OK", repository.lastUpdateRequest?.teamStatus)
    }

    @Test
    fun `updateMeeting maps meetingStatusUI completed to COMPLETED`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "05.08", "Состоялась")
        advanceUntilIdle()

        assertEquals("COMPLETED", repository.lastUpdateRequest?.status)
    }

    @Test
    fun `updateMeeting maps meetingStatusUI not happened to COMPLETED_AS_NOT_HAPPENED`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "05.08", "Не состоялась")
        advanceUntilIdle()

        assertEquals("COMPLETED_AS_NOT_HAPPENED", repository.lastUpdateRequest?.status)
    }

    @Test
    fun `updateMeeting maps unknown meetingStatusUI to SCHEDULED`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "05.08", "Что-то другое")
        advanceUntilIdle()

        assertEquals("SCHEDULED", repository.lastUpdateRequest?.status)
    }

    @Test
    fun `updateMeeting updates date via updateIsoDateWithUi`() = runTest {
        val meeting = sampleMeeting(startDate = "2024-08-05T10:00:00")
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "15.09", "Состоялась")
        advanceUntilIdle()

        assertEquals("2024-09-15T10:00:00", repository.lastUpdateRequest?.startDate)
    }

    @Test
    fun `updateMeeting sets errorMessage on update failure`() = runTest {
        val meeting = sampleMeeting()
        val repository = object : MeetingRepository {
            override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> =
                Result.success(MeetingPage(listOf(meeting), 1, 1, 0))
            override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> =
                Result.failure(Exception("Update failed"))
            override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)
            override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> = Result.success(Unit)
            override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> =
                Result.success(Unit)
        }
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "05.08", "Состоялась")
        advanceUntilIdle()

        assertEquals("Update failed", viewModel.errorMessage)
    }

    // ========== updateIsoDateWithUi tests ==========

    @Test
    fun `updateIsoDateWithUi replaces month and day correctly`() = runTest {
        val meeting = sampleMeeting(startDate = "2024-08-05T10:00:00")
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "01.12", "Состоялась")
        advanceUntilIdle()

        assertEquals("2024-12-01T10:00:00", repository.lastUpdateRequest?.startDate)
    }

    @Test
    fun `updateIsoDateWithUi pads single digit day and month`() = runTest {
        val meeting = sampleMeeting(startDate = "2024-08-05T10:00:00")
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "3.7", "Состоялась")
        advanceUntilIdle()

        assertEquals("2024-07-03T10:00:00", repository.lastUpdateRequest?.startDate)
    }

    @Test
    fun `updateIsoDateWithUi returns oldIso when uiDate has invalid format`() = runTest {
        val meeting = sampleMeeting(startDate = "2024-08-05T10:00:00")
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "invalid", "Состоялась")
        advanceUntilIdle()

        assertEquals("2024-08-05T10:00:00", repository.lastUpdateRequest?.startDate)
    }

    @Test
    fun `updateIsoDateWithUi returns oldIso when uiDate is empty`() = runTest {
        val meeting = sampleMeeting(startDate = "2024-08-05T10:00:00")
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "", "Состоялась")
        advanceUntilIdle()

        assertEquals("2024-08-05T10:00:00", repository.lastUpdateRequest?.startDate)
    }

    // ========== uploadImage tests ==========

    @Test
    fun `uploadImage does nothing when meeting is null`() = runTest {
        val repository = FakeMeetingRepositoryBig(meetings = listOf(sampleMeeting(id = "other")))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.uploadImage(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertNull(repository.lastUploadedBytes)
    }

    @Test
    fun `uploadImage calls repository uploadImage`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        val imageBytes = byteArrayOf(10, 20, 30, 40)
        viewModel.uploadImage(imageBytes)
        advanceUntilIdle()

        assertNotNull(repository.lastUploadedBytes)
        assertEquals(4, repository.lastUploadedBytes!!.size)
    }

    @Test
    fun `uploadImage sets errorMessage on failure`() = runTest {
        val meeting = sampleMeeting()
        val repository = object : MeetingRepository {
            override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> =
                Result.success(MeetingPage(listOf(meeting), 1, 1, 0))
            override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> =
                Result.success(Unit)
            override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)
            override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> =
                Result.failure(Exception("Upload error"))
            override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> =
                Result.success(Unit)
        }
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.uploadImage(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertNotNull(viewModel.errorMessage)
        assertTrue(viewModel.errorMessage!!.contains("Upload error"))
    }

    @Test
    fun `uploadImage reloads meetings after success`() = runTest {
        var loadCount = 0
        val meeting = sampleMeeting()
        val repository = object : MeetingRepository {
            override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> {
                loadCount++
                return Result.success(MeetingPage(listOf(meeting), 1, 1, 0))
            }
            override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> =
                Result.success(Unit)
            override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)
            override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> =
                Result.success(Unit)
            override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> =
                Result.success(Unit)
        }
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        assertEquals(1, loadCount) // initial load

        viewModel.uploadImage(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertTrue(loadCount >= 2) // reloaded after upload
    }

    // ========== isLoading state tests ==========

    @Test
    fun `isLoading is false after init completes`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `updateMeeting sets error on failure`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting), updateResult = Result.failure(Exception("fail")))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.updateMeeting("meeting-1", "n", "c", "Всё ок", "link", "05.08", "Состоялась")
        advanceUntilIdle()

        assertEquals("fail", viewModel.errorMessage)
    }

    @Test
    fun `uploadImage sets error on failure`() = runTest {
        val meeting = sampleMeeting()
        val repository = FakeMeetingRepositoryBig(meetings = listOf(meeting), uploadResult = Result.failure(Exception("upload fail")))
        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", FakeTeamCardRepositoryBig(sampleTeamCard()))
        advanceUntilIdle()

        viewModel.uploadImage(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertTrue(viewModel.errorMessage?.contains("upload fail") == true)
    }

    // ========== MeetingScreenUtils tests ==========

    @Test
    fun `getTeamStatusUi maps all known statuses`() {
        assertEquals("Всё ок", getTeamStatusUi("OK"))
        assertEquals("Есть проблемы", getTeamStatusUi("WITH_ISSUES"))
        assertEquals("Есть большие проблемы", getTeamStatusUi("MANY_ISSUES"))
        assertEquals("Всё ок", getTeamStatusUi("UNKNOWN"))
    }

    @Test
    fun `getMeetingStatusUi maps all known statuses`() {
        assertEquals("Состоялась", getMeetingStatusUi("COMPLETED"))
        assertEquals("Не состоялась", getMeetingStatusUi("COMPLETED_AS_NOT_HAPPENED"))
        assertEquals("Не указана", getMeetingStatusUi("SCHEDULED"))
        assertEquals("Не указана", getMeetingStatusUi("UNKNOWN"))
    }

    @Test
    fun `formatMeetingDate handles various ISO formats`() {
        assertEquals("05.08", formatMeetingDate("2024-08-05T10:00:00Z"))
        assertEquals("01.01", formatMeetingDate("2024-01-01T00:00:00Z"))
    }

    @Test
    fun `formatMeetingDate returns fallback on parse failure`() {
        assertEquals("01.01", formatMeetingDate("invalid"))
        assertEquals("01.01", formatMeetingDate(""))
    }

    @Test
    fun `isMeetingDatePassed returns true for past dates`() {
        assertTrue(isMeetingDatePassed("2020-01-01T00:00:00Z"))
    }

    @Test
    fun `isMeetingDatePassed returns false for future dates`() {
        assertFalse(isMeetingDatePassed("2099-12-31T23:59:59Z"))
    }

    @Test
    fun `isMeetingDatePassed returns false on parse failure`() {
        assertFalse(isMeetingDatePassed("invalid"))
    }

    @Test
    fun `getResultHintText returns correct hints`() {
        assertEquals("", getResultHintText(true, true))
        assertEquals("Заполните все поля встречи", getResultHintText(true, false))
        assertEquals("", getResultHintText(false, false))
        assertEquals("Результат можно отметить после даты встречи", getResultHintText(false, true))
    }

    @Test
    fun `mapTeamStatusUiToApi maps correctly`() {
        assertEquals("OK", mapTeamStatusUiToApi("Всё ок"))
        assertEquals("WITH_ISSUES", mapTeamStatusUiToApi("Есть проблемы"))
        assertEquals("MANY_ISSUES", mapTeamStatusUiToApi("Есть большие проблемы"))
        assertEquals("OK", mapTeamStatusUiToApi("Неизвестно"))
    }

    @Test
    fun `mapResultStatusUiToApi maps correctly`() {
        assertEquals("COMPLETED", mapResultStatusUiToApi("Состоялась"))
        assertEquals("COMPLETED_AS_NOT_HAPPENED", mapResultStatusUiToApi("Не состоялась"))
        assertEquals("SCHEDULED", mapResultStatusUiToApi("Запланирована"))
        assertEquals("SCHEDULED", mapResultStatusUiToApi("Неизвестно"))
    }

    // ========== Fake repositories ==========

    private class FakeMeetingRepositoryBig(
        private val meetings: List<Meeting>,
        private val updateResult: Result<Unit> = Result.success(Unit),
        private val uploadResult: Result<Unit> = Result.success(Unit)
    ) : MeetingRepository {
        var lastUpdateRequest: MeetingUpdateRequest? = null
        var lastUploadedBytes: ByteArray? = null

        override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> {
            return Result.success(MeetingPage(content = meetings, totalPages = 1, totalElements = meetings.size.toLong(), currentPage = 0))
        }

        override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> {
            lastUpdateRequest = request
            return updateResult
        }

        override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)

        override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> {
            lastUploadedBytes = fileBytes
            return uploadResult
        }

        override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> =
            Result.success(Unit)
    }

    private class FakeTeamCardRepositoryBig(
        private val teamCard: TeamCard
    ) : TeamCardRepository {
        override suspend fun getTeamById(id: String): Result<TeamCard> = Result.success(teamCard)
        override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("tracker")
        override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
        override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
        override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
        override suspend fun createTeam(request: CreateTeamRequest): Result<Unit> = Result.success(Unit)
        override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
        override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
    }
}