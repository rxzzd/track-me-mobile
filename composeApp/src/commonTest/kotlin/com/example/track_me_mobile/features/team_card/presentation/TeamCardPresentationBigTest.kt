package com.example.track_me_mobile.features.team_card.presentation

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCardPresentationBigTest {

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
    fun `TeamCardViewModel loads team and tracker full name`() = runTest {
        val team = sampleTeamCard()
        val repository = FakeTeamCardRepository(
            teamResult = Result.success(team),
            fullNameResult = Result.success("Alice Tracker")
        )
        val viewModel = TeamCardViewModel("team-1", repository)
        advanceUntilIdle()

        assertTrue(viewModel.state is TeamCardState.Success)
        val success = viewModel.state as TeamCardState.Success
        assertEquals("Alpha", success.team.name)
        assertEquals("Alice Tracker", success.trackerFullName)
    }

    @Test
    fun `TeamCardViewModel falls back when tracker full name unavailable`() = runTest {
        val repository = FakeTeamCardRepository(
            teamResult = Result.success(sampleTeamCard()),
            fullNameResult = Result.failure(Exception("404"))
        )
        val viewModel = TeamCardViewModel("team-1", repository)
        advanceUntilIdle()

        assertEquals("ФИО недоступно", (viewModel.state as TeamCardState.Success).trackerFullName)
    }

    @Test
    fun `TeamCardViewModel load failure sets error state`() = runTest {
        val viewModel = TeamCardViewModel("team-1", FakeTeamCardRepository(teamResult = Result.failure(Exception("fail"))))
        advanceUntilIdle()

        assertTrue(viewModel.state is TeamCardState.Error)
    }

    @Test
    fun `TeamCreateViewModel validates all required fields`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.submit {}
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.teamNameError)
        assertNotNull(state.trackerError)
        assertNotNull(state.streamError)
        assertNotNull(state.marketsError)
        assertNotNull(state.trlError)
        assertNotNull(state.descriptionError)
    }

    @Test
    fun `TeamCreateViewModel isLinkRegex validates URLs`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        assertTrue(viewModel.isLinkRegex("https://webinar.example.com/room"))
        assertFalse(viewModel.isLinkRegex("not-a-url"))
    }

    @Test
    fun `TeamCreateViewModel submit success for admin`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        fillValidCreateForm(viewModel)
        var success = false
        viewModel.submit { success = true }
        advanceUntilIdle()

        assertTrue(success)
        assertTrue(viewModel.state.value.isSuccess)
    }

    @Test
    fun `TeamCreateViewModel tracker role skips tracker list and preselects current user`() = runTest {
        val holder = UserInfoHolder()
        holder.save(UserInfo("tid", "tracker1", "Tracker One", null, listOf(Role.TRACKER)))
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), holder)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isTrackerRole)
        assertTrue(viewModel.state.value.trackers.isEmpty())
        assertEquals("tracker1", viewModel.state.value.selectedTracker?.username)
    }

    @Test
    fun `TeamCreateViewModel loadInitialData failure sets loadError for streams`() = runTest {
        val repository = FakeTeamCardRepository(streamsResult = Result.failure(Exception("fail")))
        val viewModel = TeamCreateViewModel(repository, adminHolder())
        advanceUntilIdle()

        assertEquals("Не удалось загрузить потоки. Попробуйте ещё раз.", viewModel.state.value.loadError)
    }

    @Test
    fun `TeamEditViewModel loads team data for admin`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Alpha", state.name)
        assertTrue(state.isAdminRole)
        assertNotNull(state.selectedTracker)
    }

    @Test
    fun `TeamEditViewModel save validates and submits for admin`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        var saved = false
        viewModel.save { saved = true }
        advanceUntilIdle()

        assertTrue(saved)
    }

    @Test
    fun `TeamEditViewModel deactivate calls deleteTeam`() = runTest {
        var deleted = false
        val repository = object : FakeTeamCardRepository() {
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> {
                deleted = true
                return Result.success(Unit)
            }
        }
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        viewModel.deactivate {}
        advanceUntilIdle()

        assertTrue(deleted)
    }

    @Test
    fun `TeamMeetingsViewModel loads and maps meetings`() = runTest {
        val meetings = listOf(
            Meeting(
                id = "m1",
                teamCardId = "team-1",
                number = "002",
                link = "https://link.com",
                startDate = "2024-08-05T10:00:00Z",
                teamStatus = "OK",
                status = "SCHEDULED",
                tasksCurrentMeeting = "current",
                tasksNextMeeting = "next",
                imageUrl = null
            ),
            Meeting(
                id = "m2",
                teamCardId = "team-1",
                number = "001",
                link = "https://link.com",
                startDate = "2024-07-01T10:00:00Z",
                teamStatus = "OK",
                status = "COMPLETED",
                tasksCurrentMeeting = "c",
                tasksNextMeeting = "n",
                imageUrl = null
            )
        )
        val viewModel = TeamMeetingsViewModel("team-1", FakeMeetingRepository(meetings))
        advanceUntilIdle()

        assertTrue(viewModel.state is TeamMeetingsState.Success)
        val list = (viewModel.state as TeamMeetingsState.Success).meetings
        assertEquals("002", list.first().number)
        assertTrue(list.first().title.contains("002"))
    }

    @Test
    fun `TeamMeetingsViewModel planNewMeeting increments meeting number`() = runTest {
        val repository = FakeMeetingRepository(
            meetings = listOf(
                Meeting("m1", "team-1", "003", "l", "2024-01-01T10:00:00Z", "OK", "SCHEDULED", "c", "n", null)
            )
        )
        val viewModel = TeamMeetingsViewModel("team-1", repository)
        advanceUntilIdle()

        viewModel.planNewMeeting("2024-09-01T10:00:00Z") {}
        advanceUntilIdle()

        assertEquals("4", repository.lastCreatedNumber)
    }

    @Test
    fun `TeamViewModel updates and resets filter data`() = runTest {
        val viewModel = TeamViewModel()

        viewModel.updateTrackerName("Alice")
        viewModel.updateStream("Stream A")
        viewModel.updateMarkets(listOf("M1"))
        viewModel.updateTrl("5-7")
        viewModel.updateDescription("Desc")

        assertEquals("Alice", viewModel.teamData.value.trackerName)
        assertEquals("Stream A", viewModel.teamData.value.stream)

        viewModel.reset()
        assertEquals(TeamFilterData(), viewModel.teamData.value)
    }

    @Test
    fun `TRL_OPTIONS contains expected ranges`() {
        assertEquals(listOf("0-2", "3-5", "6-8", "9-10"), TRL_OPTIONS)
    }

    @Test
    fun `TeamFilterData and MeetingData data classes`() {
        val filter = TeamFilterData(stream = "S", markets = listOf("M"), trl = "5", description = "D", trackerName = "T")
        assertEquals("S", filter.stream)
        val meeting = MeetingData("01.01", "Meeting")
        assertEquals("Meeting", meeting.title)
    }

    private fun fillValidCreateForm(viewModel: TeamCreateViewModel) {
        val state = viewModel.state.value
        viewModel.onTeamNameChange("Alpha Team")
        viewModel.onMeetingRoomChange("https://webinar.example.com/room")
        viewModel.onDescriptionChange("Valid description for team card")
        viewModel.onTrackerSelected(state.trackers.first())
        viewModel.onStreamSelected(state.streams.first())
        viewModel.onMarketsChanged(listOf(state.ntiMarkets.first()))
        viewModel.onTrlSelected("3-5")
    }

    private fun adminHolder(): UserInfoHolder {
        val holder = UserInfoHolder()
        holder.save(UserInfo("1", "admin", "Admin", "a@a.com", listOf(Role.ADMIN)))
        return holder
    }

    private fun sampleTeamCard() = TeamCard(
        id = "team-1",
        name = "Alpha",
        meetingRoomLink = "https://link.com",
        description = "Description",
        status = "ACTIVE",
        username = "tracker",
        enabled = true,
        ntiMarkets = listOf(NtiMarket("m1", "market", "Market")),
        readinessLevel = "5-7",
        averageGrade = 4.0,
        stream = Stream("s1", "Stream", "d", true),
        meetingsCount = 1,
        meetingsCompletedCount = 1,
        meetingsNotHappenedCount = 0
    )

    private open class FakeTeamCardRepository(
        private val teamResult: Result<TeamCard> = Result.success(
            TeamCard(
                id = "team-1",
                name = "Alpha",
                meetingRoomLink = "https://link.com",
                description = "Description long enough for validation",
                status = "ACTIVE",
                username = "tracker",
                enabled = true,
                ntiMarkets = listOf(NtiMarket("m1", "market", "Market")),
                readinessLevel = "5-7",
                averageGrade = 4.0,
                stream = Stream("s1", "Stream A", "d", true),
                meetingsCount = 1,
                meetingsCompletedCount = 1,
                meetingsNotHappenedCount = 0
            )
        ),
        private val fullNameResult: Result<String> = Result.success("Tracker"),
        private val streamsResult: Result<List<Stream>> = Result.success(listOf(Stream("s1", "Stream A", "d", true))),
        private val marketsResult: Result<List<NtiMarket>> = Result.success(listOf(NtiMarket("m1", "market", "Market"))),
        private val trackersResult: Result<List<TrackerUser>> = Result.success(
            listOf(TrackerUser("1", "tracker", "Tracker", "t@e.com", null))
        )
    ) : TeamCardRepository {
        override suspend fun getTeamById(id: String): Result<TeamCard> = teamResult
        override suspend fun getTrackerFullName(username: String): Result<String> = fullNameResult
        override suspend fun getTrackers(): Result<List<TrackerUser>> = trackersResult
        override suspend fun getStreams(): Result<List<Stream>> = streamsResult
        override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = marketsResult
        override suspend fun createTeam(request: CreateTeamRequest): Result<Unit> = Result.success(Unit)
        override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
        override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeMeetingRepository(
        private val meetings: List<Meeting> = emptyList()
    ) : MeetingRepository {
        var lastCreatedNumber: String? = null

        override suspend fun getMeetings(teamCardId: String, page: Int, size: Int): Result<MeetingPage> {
            return Result.success(MeetingPage(meetings, meetings.size, 1, 0))
        }

        override suspend fun createMeeting(teamCardId: String, startDateIso: String, number: String): Result<Unit> {
            lastCreatedNumber = number
            return Result.success(Unit)
        }

        override suspend fun updateMeeting(
            meetingId: String,
            teamCardId: String,
            request: MeetingUpdateRequest
        ): Result<Unit> = Result.success(Unit)

        override suspend fun deleteMeeting(meetingId: String): Result<Unit> = Result.success(Unit)

        override suspend fun uploadImage(meetingId: String, imageBytes: ByteArray): Result<Unit> = Result.success(Unit)
    }
}
