package com.example.track_me_mobile.features.team_card.presentation

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
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
class TeamCreateViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun adminHolder(): UserInfoHolder {
        val holder = UserInfoHolder()
        holder.save(UserInfo("1", "admin", "Admin", "a@a.com", listOf(Role.ADMIN)))
        return holder
    }

    private fun trackerHolder(): UserInfoHolder {
        val holder = UserInfoHolder()
        holder.save(UserInfo("tid", "tracker1", "Tracker One", null, listOf(Role.TRACKER)))
        return holder
    }

    private fun fakeRepo(
        streamsResult: Result<List<Stream>> = Result.success(listOf(Stream("s1", "Stream A", "d", true))),
        marketsResult: Result<List<NtiMarket>> = Result.success(listOf(NtiMarket("m1", "market", "Market"))),
        trackersResult: Result<List<TrackerUser>> = Result.success(
            listOf(TrackerUser("1", "tracker", "Tracker", "t@e.com", null))
        ),
        createResult: Result<Unit> = Result.success(Unit)
    ) = object : TeamCardRepository {
        override suspend fun getTeamById(id: String): Result<TeamCard> = Result.failure(Exception("not used"))
        override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("")
        override suspend fun getTrackers(): Result<List<TrackerUser>> = trackersResult
        override suspend fun getStreams(): Result<List<Stream>> = streamsResult
        override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = marketsResult
        override suspend fun createTeam(request: CreateTeamRequest): Result<Unit> = createResult
        override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
        override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
    }

    // ========== loadInitialData tests ==========

    @Test
    fun `loadInitialData success for admin loads all data`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.loadError)
        assertEquals(1, state.streams.size)
        assertEquals(1, state.ntiMarkets.size)
        assertEquals(1, state.trackers.size)
        assertFalse(state.isTrackerRole)
    }

    @Test
    fun `loadInitialData failure for streams sets loadError`() = runTest {
        val viewModel = TeamCreateViewModel(
            fakeRepo(streamsResult = Result.failure(Exception("streams fail"))),
            adminHolder()
        )
        advanceUntilIdle()

        assertEquals("Не удалось загрузить потоки. Попробуйте ещё раз.", viewModel.state.value.loadError)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `loadInitialData failure for markets sets loadError`() = runTest {
        val viewModel = TeamCreateViewModel(
            fakeRepo(marketsResult = Result.failure(Exception("markets fail"))),
            adminHolder()
        )
        advanceUntilIdle()

        assertEquals("Не удалось загрузить рынки НТИ. Попробуйте ещё раз.", viewModel.state.value.loadError)
    }

    @Test
    fun `loadInitialData failure for trackers sets loadError for admin`() = runTest {
        val viewModel = TeamCreateViewModel(
            fakeRepo(trackersResult = Result.failure(Exception("trackers fail"))),
            adminHolder()
        )
        advanceUntilIdle()

        assertEquals("Не удалось загрузить список трекеров. Попробуйте ещё раз.", viewModel.state.value.loadError)
    }

    @Test
    fun `loadInitialData tracker role skips trackers and preselects current user`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), trackerHolder())
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isTrackerRole)
        assertTrue(state.trackers.isEmpty())
        assertEquals("tracker1", state.selectedTracker?.username)
    }

    @Test
    fun `loadInitialData tracker role does not fail on trackers error`() = runTest {
        val viewModel = TeamCreateViewModel(
            fakeRepo(trackersResult = Result.failure(Exception("ignored"))),
            trackerHolder()
        )
        advanceUntilIdle()

        assertNull(viewModel.state.value.loadError)
        assertTrue(viewModel.state.value.isTrackerRole)
    }

    // ========== field change tests ==========

    @Test
    fun `onTeamNameChange updates teamName and clears error`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("New Team")
        assertEquals("New Team", viewModel.state.value.teamName)
        assertNull(viewModel.state.value.teamNameError)
    }

    @Test
    fun `onMeetingRoomChange updates meetingRoomLink and clears error`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onMeetingRoomChange("https://link.com")
        assertEquals("https://link.com", viewModel.state.value.meetingRoomLink)
        assertNull(viewModel.state.value.meetingRoomLinkError)
    }

    @Test
    fun `onDescriptionChange updates description and clears error`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onDescriptionChange("New description")
        assertEquals("New description", viewModel.state.value.description)
        assertNull(viewModel.state.value.descriptionError)
    }

    @Test
    fun `onTrackerSelected updates tracker and clears error`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        val tracker = TrackerUser("2", "t2", "Tracker 2", "e@e.com", null)
        viewModel.onTrackerSelected(tracker)
        assertEquals("t2", viewModel.state.value.selectedTracker?.username)
        assertNull(viewModel.state.value.trackerError)
    }

    @Test
    fun `onStreamSelected updates stream and clears error`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        val stream = Stream("s2", "Stream B", "d", true)
        viewModel.onStreamSelected(stream)
        assertEquals("s2", viewModel.state.value.selectedStream?.id)
        assertNull(viewModel.state.value.streamError)
    }

    @Test
    fun `onMarketsChanged updates markets and clears error`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        val markets = listOf(NtiMarket("m2", "m2", "Market 2"))
        viewModel.onMarketsChanged(markets)
        assertEquals(1, viewModel.state.value.selectedMarkets.size)
        assertNull(viewModel.state.value.marketsError)
    }

    @Test
    fun `onTrlSelected updates trl and clears error`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onTrlSelected("6-8")
        assertEquals("6-8", viewModel.state.value.selectedTrl)
        assertNull(viewModel.state.value.trlError)
    }

    // ========== validation tests ==========

    @Test
    fun `submit with blank name shows teamNameError`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Введите название команды", viewModel.state.value.teamNameError)
    }

    @Test
    fun `submit with short name shows teamNameError`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("A")
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Минимум 2 символа", viewModel.state.value.teamNameError)
    }

    @Test
    fun `submit with long name shows teamNameError`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("A".repeat(101))
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Максимум 100 символов", viewModel.state.value.teamNameError)
    }

    @Test
    fun `submit with blank description shows descriptionError`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Valid Name")
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Добавьте описание команды", viewModel.state.value.descriptionError)
    }

    @Test
    fun `submit with short description shows descriptionError`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Valid Name")
        viewModel.onDescriptionChange("Short")
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Минимум 10 символов", viewModel.state.value.descriptionError)
    }

    @Test
    fun `submit with invalid link shows meetingRoomLinkError`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Valid Name")
        viewModel.onDescriptionChange("Valid description long enough")
        viewModel.onMeetingRoomChange("not-a-url")
        viewModel.submit {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.meetingRoomLinkError)
    }

    @Test
    fun `submit with valid link does not show link error`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Valid Name")
        viewModel.onDescriptionChange("Valid description long enough")
        viewModel.onMeetingRoomChange("https://webinar.example.com/room")
        viewModel.onTrackerSelected(viewModel.state.value.trackers.first())
        viewModel.onStreamSelected(viewModel.state.value.streams.first())
        viewModel.onMarketsChanged(listOf(viewModel.state.value.ntiMarkets.first()))
        viewModel.onTrlSelected("3-5")
        viewModel.submit {}
        advanceUntilIdle()

        assertNull(viewModel.state.value.meetingRoomLinkError)
    }

    @Test
    fun `submit with empty markets shows marketsError`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Valid Name")
        viewModel.onDescriptionChange("Valid description long enough")
        viewModel.onTrackerSelected(viewModel.state.value.trackers.first())
        viewModel.onStreamSelected(viewModel.state.value.streams.first())
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Выберите хотя бы один рынок НТИ", viewModel.state.value.marketsError)
    }

    @Test
    fun `submit with empty TRL shows trlError`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Valid Name")
        viewModel.onDescriptionChange("Valid description long enough")
        viewModel.onTrackerSelected(viewModel.state.value.trackers.first())
        viewModel.onStreamSelected(viewModel.state.value.streams.first())
        viewModel.onMarketsChanged(listOf(viewModel.state.value.ntiMarkets.first()))
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Выберите TRL", viewModel.state.value.trlError)
    }

    @Test
    fun `submit with all valid data calls repository`() = runTest {
        var createCalled = false
        val repo = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.failure(Exception(""))
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(
                listOf(TrackerUser("1", "tracker", "Tracker", "t@e.com", null))
            )
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(
                listOf(Stream("s1", "Stream A", "d", true))
            )
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(
                listOf(NtiMarket("m1", "market", "Market"))
            )
            override suspend fun createTeam(request: CreateTeamRequest): Result<Unit> {
                createCalled = true
                assertEquals("Valid Name", request.name)
                assertEquals("tracker", request.trackerUsername)
                assertEquals("s1", request.streamId)
                assertEquals("3-5", request.readinessLevel)
                return Result.success(Unit)
            }
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = TeamCreateViewModel(repo, adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Valid Name")
        viewModel.onDescriptionChange("Valid description long enough")
        viewModel.onMeetingRoomChange("https://webinar.example.com/room")
        viewModel.onTrackerSelected(viewModel.state.value.trackers.first())
        viewModel.onStreamSelected(viewModel.state.value.streams.first())
        viewModel.onMarketsChanged(listOf(viewModel.state.value.ntiMarkets.first()))
        viewModel.onTrlSelected("3-5")

        var success = false
        viewModel.submit { success = true }
        advanceUntilIdle()

        assertTrue(createCalled)
        assertTrue(success)
        assertTrue(viewModel.state.value.isSuccess)
    }

    @Test
    fun `submit failure sets submitError`() = runTest {
        val repo = fakeRepo(createResult = Result.failure(Exception("Creation failed")))
        val viewModel = TeamCreateViewModel(repo, adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Valid Name")
        viewModel.onDescriptionChange("Valid description long enough")
        viewModel.onMeetingRoomChange("https://webinar.example.com/room")
        viewModel.onTrackerSelected(viewModel.state.value.trackers.first())
        viewModel.onStreamSelected(viewModel.state.value.streams.first())
        viewModel.onMarketsChanged(listOf(viewModel.state.value.ntiMarkets.first()))
        viewModel.onTrlSelected("3-5")

        viewModel.submit {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.submitError)
        assertTrue(viewModel.state.value.submitError!!.contains("Creation failed"))
        assertFalse(viewModel.state.value.isSubmitting)
    }

    // ========== retry tests ==========

    @Test
    fun `retry reloads initial data after failure`() = runTest {
        var failStreams = true
        val repo = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.failure(Exception(""))
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(
                listOf(TrackerUser("1", "tracker", "Tracker", "t@e.com", null))
            )
            override suspend fun getStreams(): Result<List<Stream>> {
                return if (failStreams) Result.failure(Exception("fail")) else Result.success(
                    listOf(Stream("s1", "Stream A", "d", true))
                )
            }
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(
                listOf(NtiMarket("m1", "market", "Market"))
            )
            override suspend fun createTeam(request: CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = TeamCreateViewModel(repo, adminHolder())
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.loadError)

        failStreams = false
        viewModel.retry()
        advanceUntilIdle()

        assertNull(viewModel.state.value.loadError)
        assertEquals(1, viewModel.state.value.streams.size)
    }

    // ========== clearSubmitError tests ==========

    @Test
    fun `clearSubmitError clears submit error`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(createResult = Result.failure(Exception("fail"))), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Valid Name")
        viewModel.onDescriptionChange("Valid description long enough")
        viewModel.onMeetingRoomChange("https://webinar.example.com/room")
        viewModel.onTrackerSelected(viewModel.state.value.trackers.first())
        viewModel.onStreamSelected(viewModel.state.value.streams.first())
        viewModel.onMarketsChanged(listOf(viewModel.state.value.ntiMarkets.first()))
        viewModel.onTrlSelected("3-5")
        viewModel.submit {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.submitError)

        viewModel.clearSubmitError()
        assertNull(viewModel.state.value.submitError)
    }

    // ========== isLinkRegex tests ==========

    @Test
    fun `isLinkRegex accepts valid URLs`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        assertTrue(viewModel.isLinkRegex("https://webinar.example.com/room"))
        assertTrue(viewModel.isLinkRegex("http://example.com"))
        assertTrue(viewModel.isLinkRegex("ftp://files.example.com"))
        assertTrue(viewModel.isLinkRegex("https://example.com/path?query=1&param=2"))
    }

    @Test
    fun `isLinkRegex rejects invalid URLs`() = runTest {
        val viewModel = TeamCreateViewModel(fakeRepo(), adminHolder())
        advanceUntilIdle()

        assertFalse(viewModel.isLinkRegex("not-a-url"))
        assertFalse(viewModel.isLinkRegex(""))
        assertFalse(viewModel.isLinkRegex("www.example.com"))
    }

    // ========== TRL_OPTIONS test ==========

    @Test
    fun `TRL_OPTIONS contains expected ranges`() {
        assertEquals(listOf("0-2", "3-5", "6-8", "9-10"), TRL_OPTIONS)
    }
}