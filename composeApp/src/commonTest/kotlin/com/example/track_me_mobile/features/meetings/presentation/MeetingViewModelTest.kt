package com.example.track_me_mobile.features.meetings.presentation

import com.example.track_me_mobile.features.meetings.domain.MeetingRepository
import com.example.track_me_mobile.features.meetings.domain.models.Meeting
import com.example.track_me_mobile.features.meetings.domain.models.MeetingPage
import com.example.track_me_mobile.features.meetings.domain.models.MeetingUpdateRequest
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
import com.example.track_me_mobile.features.team_card.domain.models.TrackerUser
import com.example.track_me_mobile.features.teams.domain.models.NtiMarket
import com.example.track_me_mobile.features.teams.domain.models.Stream
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingViewModelTest {

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
    fun `init loads meeting and team card`() = runTest {
        val meeting = Meeting(
            id = "meeting-1",
            teamCardId = "team-1",
            number = "001",
            link = "https://meeting",
            startDate = "2024-08-05T10:00:00",
            teamStatus = "OK",
            status = "SCHEDULED",
            tasksCurrentMeeting = "current",
            tasksNextMeeting = "next",
            imageUrl = null
        )
        val repository = FakeMeetingRepository(
            meetings = listOf(meeting)
        )
        val teamCardRepository = FakeTeamCardRepository(
            teamCard = TeamCard(
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
        )

        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", teamCardRepository)

        advanceUntilIdle()

        assertNotNull(viewModel.meeting)
        assertEquals("room-link", viewModel.meetingRoomLink)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `updateMeeting sends mapped request and reloads meetings`() = runTest {
        val meeting = Meeting(
            id = "meeting-1",
            teamCardId = "team-1",
            number = "001",
            link = "https://meeting",
            startDate = "2024-08-05T10:00:00",
            teamStatus = "OK",
            status = "SCHEDULED",
            tasksCurrentMeeting = "current",
            tasksNextMeeting = "next",
            imageUrl = null
        )
        val repository = FakeMeetingRepository(
            meetings = listOf(meeting)
        )
        val teamCardRepository = FakeTeamCardRepository(
            teamCard = TeamCard(
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
        )

        val viewModel = MeetingViewModel(repository, "meeting-1", "team-1", teamCardRepository)
        advanceUntilIdle()

        viewModel.updateMeeting(
            meetingId = "meeting-1",
            tasksNext = "next tasks",
            tasksCurrent = "current tasks",
            teamStatusUI = "Есть проблемы",
            link = "https://updated",
            uiDate = "01.08",
            meetingStatusUI = "Состоялась"
        )

        advanceUntilIdle()

        assertEquals("https://updated", repository.lastUpdateRequest?.link)
        assertEquals("COMPLETED", repository.lastUpdateRequest?.status)
        assertEquals("WITH_ISSUES", repository.lastUpdateRequest?.teamStatus)
    }

    private class FakeMeetingRepository(
        private val meetings: List<Meeting>
    ) : MeetingRepository {
        var lastUpdateRequest: MeetingUpdateRequest? = null

        override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> {
            return Result.success(MeetingPage(content = meetings, totalPages = 1, totalElements = meetings.size.toLong(), currentPage = 0))
        }

        override suspend fun updateMeeting(meetingId: String, teamCardId: String, request: MeetingUpdateRequest): Result<Unit> {
            lastUpdateRequest = request
            return Result.success(Unit)
        }

        override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)
        override suspend fun uploadImage(meetingId: String, fileBytes: ByteArray): Result<Unit> = Result.success(Unit)
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
}
