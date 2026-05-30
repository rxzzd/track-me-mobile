package com.example.track_me_mobile.features.team_card.presentation

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCardViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== TeamCardViewModel Tests ====================

    @Test
    fun `TeamCardViewModel init loads team and sets Success state`() = runTest {
        val team = sampleTeamCard()
        val repository = FakeTeamCardRepository(team = team)

        val viewModel = TeamCardViewModel("team-1", repository)
        advanceUntilIdle()

        val state = viewModel.state
        assertIs<TeamCardState.Success>(state)
        assertEquals("team-1", state.team.id)
        assertEquals("tracker-fullname", state.trackerFullName)
    }

    @Test
    fun `TeamCardViewModel getTrackerFullName failure uses fallback`() = runTest {
        val team = sampleTeamCard()
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.success(team)
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.failure(Exception("not found"))
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }

        val viewModel = TeamCardViewModel("team-1", repository)
        advanceUntilIdle()

        val state = viewModel.state as TeamCardState.Success
        assertEquals("ФИО недоступно", state.trackerFullName)
    }

    @Test
    fun `TeamCardViewModel load failure sets Error state`() = runTest {
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.failure(Exception("network error"))
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("name")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }

        val viewModel = TeamCardViewModel("team-1", repository)
        advanceUntilIdle()

        assertIs<TeamCardState.Error>(viewModel.state)
    }

    @Test
    fun `TeamCardViewModel loadTeam is idempotent`() = runTest {
        var callCount = 0
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> {
                callCount++
                return Result.success(sampleTeamCard())
            }
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("name")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }

        val viewModel = TeamCardViewModel("team-1", repository)
        advanceUntilIdle()
        assertEquals(1, callCount)

        viewModel.loadTeam()
        advanceUntilIdle()
        assertEquals(2, callCount)
    }

    // ==================== TeamEditViewModel Tests ====================

    @Test
    fun `TeamEditViewModel init loads team data for admin`() = runTest {
        val team = sampleTeamCard()
        val markets = listOf(NtiMarket("m1", "Market A", "Market A"))
        val trackers = listOf(TrackerUser("tracker1", "tracker1", "Tracker One", "t@t.com", null))
        val streams = listOf(Stream("s1", "Stream A", "desc", true))
        val repository = FakeTeamCardRepository(team = team, markets = markets, trackers = trackers, streams = streams)
        val holder = adminHolder()

        val viewModel = TeamEditViewModel("team-1", repository, holder)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.loadError)
        assertEquals("Test Team", state.name)
        assertEquals("Tracker One", state.selectedTracker?.fullName)
        assertEquals("Stream A", state.selectedStream?.name)
        assertTrue(state.isAdminRole)
    }

    @Test
    fun `TeamEditViewModel init loads team data for non-admin`() = runTest {
        val team = sampleTeamCard()
        val markets = listOf(NtiMarket("m1", "Market A", "Market A"))
        val repository = FakeTeamCardRepository(team = team, markets = markets)
        val holder = UserInfoHolder().apply {
            save(UserInfo(id = "1", username = "tracker", fullName = "Tracker", email = "t@t.com", roles = listOf(Role.TRACKER)))
        }

        val viewModel = TeamEditViewModel("team-1", repository, holder)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.loadError)
        assertEquals("Test Team", state.name)
        assertNull(state.selectedTracker) // non-admin doesn't load trackers
        assertFalse(state.isAdminRole)
    }

    @Test
    fun `TeamEditViewModel getTeamById failure sets loadError`() = runTest {
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.failure(Exception("not found"))
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("name")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }
        val holder = adminHolder()

        val viewModel = TeamEditViewModel("team-1", repository, holder)
        advanceUntilIdle()

        assertEquals("Не удалось загрузить карточку команды", viewModel.state.value.loadError)
    }

    @Test
    fun `TeamEditViewModel getNtiMarkets failure sets loadError`() = runTest {
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.success(sampleTeamCard())
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("name")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.failure(Exception("markets fail"))
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }
        val holder = adminHolder()

        val viewModel = TeamEditViewModel("team-1", repository, holder)
        advanceUntilIdle()

        assertEquals("Не удалось загрузить рынки НТИ", viewModel.state.value.loadError)
    }

    @Test
    fun `TeamEditViewModel getTrackers failure for admin sets loadError`() = runTest {
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.success(sampleTeamCard())
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("name")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.failure(Exception("trackers fail"))
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }
        val holder = adminHolder()

        val viewModel = TeamEditViewModel("team-1", repository, holder)
        advanceUntilIdle()

        assertEquals("Не удалось загрузить трекеров", viewModel.state.value.loadError)
    }

    @Test
    fun `TeamEditViewModel getStreams failure for admin sets loadError`() = runTest {
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.success(sampleTeamCard())
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("name")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.failure(Exception("streams fail"))
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }
        val holder = adminHolder()

        val viewModel = TeamEditViewModel("team-1", repository, holder)
        advanceUntilIdle()

        assertEquals("Не удалось загрузить потоки", viewModel.state.value.loadError)
    }

    @Test
    fun `TeamEditViewModel retry reloads data`() = runTest {
        var callCount = 0
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> {
                callCount++
                return Result.success(sampleTeamCard())
            }
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("name")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }
        val holder = adminHolder()

        val viewModel = TeamEditViewModel("team-1", repository, holder)
        advanceUntilIdle()
        assertEquals(1, callCount)

        viewModel.retry()
        advanceUntilIdle()
        assertEquals(2, callCount)
    }

    // ==================== TeamEditViewModel field changes ====================

    @Test
    fun `TeamEditViewModel onNameChange updates name and clears error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onNameChange("New Name")
        assertEquals("New Name", viewModel.state.value.name)
        assertNull(viewModel.state.value.nameError)
    }

    @Test
    fun `TeamEditViewModel onMeetingRoomLinkChange updates link and clears error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onMeetingRoomLinkChange("https://new-link")
        assertEquals("https://new-link", viewModel.state.value.meetingRoomLink)
        assertNull(viewModel.state.value.meetingRoomLinkError)
    }

    @Test
    fun `TeamEditViewModel onDescriptionChange updates description and clears error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onDescriptionChange("New description")
        assertEquals("New description", viewModel.state.value.description)
        assertNull(viewModel.state.value.descriptionError)
    }

    @Test
    fun `TeamEditViewModel onTrackerSelected updates tracker and clears error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        val tracker = TrackerUser("t1", "tracker1", "Tracker", "e@e.com", null)
        viewModel.onTrackerSelected(tracker)
        assertEquals("tracker1", viewModel.state.value.selectedTracker?.username)
        assertNull(viewModel.state.value.trackerError)
    }

    @Test
    fun `TeamEditViewModel onStreamSelected updates stream and clears error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        val stream = Stream("s1", "Stream", "desc", true)
        viewModel.onStreamSelected(stream)
        assertEquals("s1", viewModel.state.value.selectedStream?.id)
        assertNull(viewModel.state.value.streamError)
    }

    @Test
    fun `TeamEditViewModel onMarketsChanged updates markets and clears error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        val markets = listOf(NtiMarket("m1", "M", "M"))
        viewModel.onMarketsChanged(markets)
        assertEquals(1, viewModel.state.value.selectedMarkets.size)
        assertNull(viewModel.state.value.marketsError)
    }

    @Test
    fun `TeamEditViewModel onTrlSelected updates TRL and clears error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onTrlSelected("6-8")
        assertEquals("6-8", viewModel.state.value.selectedTrl)
        assertNull(viewModel.state.value.trlError)
    }

    // ==================== TeamEditViewModel validation ====================

    @Test
    fun `TeamEditViewModel save with blank name shows error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onNameChange("")
        viewModel.save {}

        assertEquals("Введите название", viewModel.state.value.nameError)
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun `TeamEditViewModel save with short name shows error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onNameChange("A")
        viewModel.save {}

        assertEquals("Минимум 2 символа", viewModel.state.value.nameError)
    }

    @Test
    fun `TeamEditViewModel save with long name shows error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onNameChange("A".repeat(101))
        viewModel.save {}

        assertEquals("Максимум 100 символов", viewModel.state.value.nameError)
    }

    @Test
    fun `TeamEditViewModel save with blank description shows error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onDescriptionChange("")
        viewModel.save {}

        assertEquals("Добавьте описание", viewModel.state.value.descriptionError)
    }

    @Test
    fun `TeamEditViewModel save with short description shows error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onDescriptionChange("Short")
        viewModel.save {}

        assertEquals("Минимум 10 символов", viewModel.state.value.descriptionError)
    }

    @Test
    fun `TeamEditViewModel save with empty markets shows error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onMarketsChanged(emptyList())
        viewModel.save {}

        assertEquals("Выберите хотя бы один рынок НТИ", viewModel.state.value.marketsError)
    }

    @Test
    fun `TeamEditViewModel save with blank TRL shows error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onTrlSelected("")
        viewModel.save {}

        assertEquals("Выберите TRL", viewModel.state.value.trlError)
    }

    @Test
    fun `TeamEditViewModel save with invalid link shows error`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())
        advanceUntilIdle()

        viewModel.onMeetingRoomLinkChange("not-a-url")
        viewModel.save {}

        assertNotNull(viewModel.state.value.meetingRoomLinkError)
        assertTrue(viewModel.state.value.meetingRoomLinkError!!.contains("Ссылка"))
    }

    @Test
    fun `TeamEditViewModel save with valid data calls repository`() = runTest {
        val repository = FakeTeamCardRepository(team = sampleTeamCard(), markets = listOf(NtiMarket("m1", "M", "M")))
        val holder = adminHolder()
        val viewModel = TeamEditViewModel("team-1", repository, holder)
        advanceUntilIdle()

        viewModel.onNameChange("Valid Name")
        viewModel.onDescriptionChange("A".repeat(10))
        viewModel.onMarketsChanged(listOf(NtiMarket("m1", "M", "M")))
        viewModel.onTrlSelected("6-8")

        var successCalled = false
        viewModel.save { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertNotNull(repository.lastUpdateRequest)
        assertEquals("Valid Name", repository.lastUpdateRequest!!.name)
    }

    @Test
    fun `TeamEditViewModel save failure sets submitError`() = runTest {
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.success(sampleTeamCard())
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("name")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(listOf(NtiMarket("m1", "M", "M")))
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.failure(Exception("save failed"))
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        viewModel.onNameChange("Valid Name")
        viewModel.onDescriptionChange("A".repeat(10))
        viewModel.onMarketsChanged(listOf(NtiMarket("m1", "M", "M")))
        viewModel.onTrlSelected("6-8")

        viewModel.save {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.submitError)
        assertTrue(viewModel.state.value.submitError!!.contains("Ошибка"))
    }

    // ==================== TeamEditViewModel deactivate ====================

    @Test
    fun `TeamEditViewModel deactivate calls repository`() = runTest {
        val repository = FakeTeamCardRepository(team = sampleTeamCard())
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        var successCalled = false
        viewModel.deactivate { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals("team-1", repository.lastDeletedTeamId)
    }

    @Test
    fun `TeamEditViewModel deactivate failure sets deleteError`() = runTest {
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.success(sampleTeamCard())
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("name")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.failure(Exception("delete failed"))
        }
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        viewModel.deactivate {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.deleteError)
    }

    @Test
    fun `TeamEditViewModel deactivate when no originalTeam does nothing`() = runTest {
        val repository = object : TeamCardRepository {
            override suspend fun getTeamById(id: String): Result<TeamCard> = Result.failure(Exception("fail"))
            override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("name")
            override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(emptyList())
            override suspend fun getStreams(): Result<List<Stream>> = Result.success(emptyList())
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = Result.success(Unit)
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        var successCalled = false
        viewModel.deactivate { successCalled = true }
        advanceUntilIdle()

        assertFalse(successCalled)
    }

    // ==================== isLinkRegex ====================

    @Test
    fun `isLinkRegex validates URLs correctly`() {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(team = sampleTeamCard()), adminHolder())

        assertTrue(viewModel.isLinkRegex("https://webinar.tusur.ru/b/abc123"))
        assertTrue(viewModel.isLinkRegex("http://example.com"))
        assertTrue(viewModel.isLinkRegex("ftp://files.example.com"))
        assertFalse(viewModel.isLinkRegex("not-a-url"))
        assertFalse(viewModel.isLinkRegex(""))
        assertFalse(viewModel.isLinkRegex("www.example.com"))
    }

    // ==================== Helpers ====================

    companion object {
        private fun sampleTeamCard() = TeamCard(
            id = "team-1",
            name = "Test Team",
            meetingRoomLink = "https://meeting",
            description = "A".repeat(10),
            status = "ACTIVE",
            username = "tracker1",
            enabled = true,
            ntiMarkets = listOf(NtiMarket("m1", "Market A", "Market A")),
            readinessLevel = "6-8",
            averageGrade = 4.0,
            stream = Stream("s1", "Stream A", "desc", true),
            meetingsCount = 2,
            meetingsCompletedCount = 1,
            meetingsNotHappenedCount = 0
        )
    }

    private fun adminHolder(): UserInfoHolder {
        return UserInfoHolder().apply {
            save(UserInfo(id = "1", username = "admin", fullName = "Admin", email = "admin@test.com", roles = listOf(Role.ADMIN)))
        }
    }

    private open class FakeTeamCardRepository(
        private val team: TeamCard = sampleTeamCard(),
        private val markets: List<NtiMarket> = emptyList(),
        private val trackers: List<TrackerUser> = emptyList(),
        private val streams: List<Stream> = emptyList()
    ) : TeamCardRepository {
        var lastUpdateRequest: UpdateTeamRequest? = null
        var lastDeletedTeamId: String? = null

        override suspend fun getTeamById(id: String): Result<TeamCard> = Result.success(team)
        override suspend fun getTrackerFullName(username: String): Result<String> = Result.success("tracker-fullname")
        override suspend fun getTrackers(): Result<List<TrackerUser>> = Result.success(trackers)
        override suspend fun getStreams(): Result<List<Stream>> = Result.success(streams)
        override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(markets)
        override suspend fun createTeam(request: com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest): Result<Unit> = Result.success(Unit)
        override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> {
            lastUpdateRequest = request
            return Result.success(Unit)
        }
        override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> {
            lastDeletedTeamId = teamId
            return Result.success(Unit)
        }
    }
}