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
class TeamCardComponentsUiTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== TeamCreateViewModel Additional Edge Cases ====================

    @Test
    fun `TeamCreateViewModel validate blank team name`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("")
        viewModel.onDescriptionChange("Valid description for team card")
        viewModel.onMeetingRoomChange("https://webinar.example.com/room")
        viewModel.onTrackerSelected(viewModel.state.value.trackers.first())
        viewModel.onStreamSelected(viewModel.state.value.streams.first())
        viewModel.onMarketsChanged(listOf(viewModel.state.value.ntiMarkets.first()))
        viewModel.onTrlSelected("3-5")

        viewModel.submit {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.teamNameError)
        assertEquals("Введите название команды", viewModel.state.value.teamNameError)
    }

    @Test
    fun `TeamCreateViewModel validate short team name`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("A")
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Минимум 2 символа", viewModel.state.value.teamNameError)
    }

    @Test
    fun `TeamCreateViewModel validate long team name`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("A".repeat(101))
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Максимум 100 символов", viewModel.state.value.teamNameError)
    }

    @Test
    fun `TeamCreateViewModel validate blank description`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Alpha Team")
        viewModel.onDescriptionChange("")
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Добавьте описание команды", viewModel.state.value.descriptionError)
    }

    @Test
    fun `TeamCreateViewModel validate short description`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Alpha Team")
        viewModel.onDescriptionChange("Short")
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Минимум 10 символов", viewModel.state.value.descriptionError)
    }

    @Test
    fun `TeamCreateViewModel validate long description`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Alpha Team")
        viewModel.onDescriptionChange("A".repeat(2001))
        viewModel.submit {}
        advanceUntilIdle()

        assertEquals("Максимум 2000 символов", viewModel.state.value.descriptionError)
    }

    @Test
    fun `TeamCreateViewModel validate invalid meeting room link`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Alpha Team")
        viewModel.onDescriptionChange("Valid description for team card")
        viewModel.onMeetingRoomChange("not-a-url")
        viewModel.submit {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.meetingRoomLinkError)
    }

    @Test
    fun `TeamCreateViewModel validate empty meeting room link is ok`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onTeamNameChange("Alpha Team")
        viewModel.onDescriptionChange("Valid description for team card")
        viewModel.onMeetingRoomChange("")
        viewModel.onTrackerSelected(viewModel.state.value.trackers.first())
        viewModel.onStreamSelected(viewModel.state.value.streams.first())
        viewModel.onMarketsChanged(listOf(viewModel.state.value.ntiMarkets.first()))
        viewModel.onTrlSelected("3-5")

        var success = false
        viewModel.submit { success = true }
        advanceUntilIdle()

        assertTrue(success)
        assertNull(viewModel.state.value.meetingRoomLinkError)
    }

    @Test
    fun `TeamCreateViewModel isLinkRegex validates various URL formats`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        assertTrue(viewModel.isLinkRegex("https://webinar.tusur.ru/b/abc123"))
        assertTrue(viewModel.isLinkRegex("http://example.com"))
        assertTrue(viewModel.isLinkRegex("ftp://files.example.com"))
        assertTrue(viewModel.isLinkRegex("https://example.com/path?query=param&other=value"))
        assertFalse(viewModel.isLinkRegex(""))
        assertFalse(viewModel.isLinkRegex("just-text"))
        assertFalse(viewModel.isLinkRegex("www.example.com"))
    }

    @Test
    fun `TeamCreateViewModel loadInitialData failure for markets`() = runTest {
        val repository = FakeTeamCardRepository(
            streamsResult = Result.success(listOf(Stream("s1", "Stream A", "d", true))),
            marketsResult = Result.failure(Exception("fail"))
        )
        val viewModel = TeamCreateViewModel(repository, adminHolder())
        advanceUntilIdle()

        assertEquals("Не удалось загрузить рынки НТИ. Попробуйте ещё раз.", viewModel.state.value.loadError)
    }

    @Test
    fun `TeamCreateViewModel loadInitialData failure for trackers`() = runTest {
        val repository = FakeTeamCardRepository(
            streamsResult = Result.success(listOf(Stream("s1", "Stream A", "d", true))),
            marketsResult = Result.success(listOf(NtiMarket("m1", "market", "Market"))),
            trackersResult = Result.failure(Exception("fail"))
        )
        val viewModel = TeamCreateViewModel(repository, adminHolder())
        advanceUntilIdle()

        assertEquals("Не удалось загрузить список трекеров. Попробуйте ещё раз.", viewModel.state.value.loadError)
    }

    @Test
    fun `TeamCreateViewModel submit failure sets error`() = runTest {
        val repository = FakeTeamCardRepository(createResult = Result.failure(Exception("API error")))
        val viewModel = TeamCreateViewModel(repository, adminHolder())
        advanceUntilIdle()

        fillValidCreateForm(viewModel)
        viewModel.submit {}
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSubmitting)
        assertNotNull(viewModel.state.value.submitError)
    }

    @Test
    fun `TeamCreateViewModel clearSubmitError resets error`() = runTest {
        val repository = FakeTeamCardRepository(createResult = Result.failure(Exception("API error")))
        val viewModel = TeamCreateViewModel(repository, adminHolder())
        advanceUntilIdle()

        fillValidCreateForm(viewModel)
        viewModel.submit {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.submitError)

        viewModel.clearSubmitError()
        assertNull(viewModel.state.value.submitError)
    }

    @Test
    fun `TeamCreateViewModel retry reloads data`() = runTest {
        var callCount = 0
        val repository = object : FakeTeamCardRepository() {
            override suspend fun getStreams(): Result<List<Stream>> {
                callCount++
                return Result.success(listOf(Stream("s1", "Stream A", "d", true)))
            }
        }
        val viewModel = TeamCreateViewModel(repository, adminHolder())
        advanceUntilIdle()

        assertEquals(1, callCount)

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(2, callCount)
    }

    @Test
    fun `TeamCreateViewModel field changes clear errors`() = runTest {
        val viewModel = TeamCreateViewModel(FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.submit {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.teamNameError)

        viewModel.onTeamNameChange("New Name")
        assertNull(viewModel.state.value.teamNameError)
    }

    // ==================== TeamEditViewModel Additional Edge Cases ====================

    @Test
    fun `TeamEditViewModel load failure sets error`() = runTest {
        val repository = FakeTeamCardRepository(teamResult = Result.failure(Exception("fail")))
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNotNull(viewModel.state.value.loadError)
    }

    @Test
    fun `TeamEditViewModel validate blank name`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onNameChange("")
        viewModel.save {}
        advanceUntilIdle()

        assertEquals("Введите название", viewModel.state.value.nameError)
    }

    @Test
    fun `TeamEditViewModel validate short name`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onNameChange("A")
        viewModel.save {}
        advanceUntilIdle()

        assertEquals("Минимум 2 символа", viewModel.state.value.nameError)
    }

    @Test
    fun `TeamEditViewModel validate long name`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onNameChange("A".repeat(101))
        viewModel.save {}
        advanceUntilIdle()

        assertEquals("Максимум 100 символов", viewModel.state.value.nameError)
    }

    @Test
    fun `TeamEditViewModel validate blank description`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onDescriptionChange("")
        viewModel.save {}
        advanceUntilIdle()

        assertEquals("Добавьте описание", viewModel.state.value.descriptionError)
    }

    @Test
    fun `TeamEditViewModel validate short description`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onDescriptionChange("Short")
        viewModel.save {}
        advanceUntilIdle()

        assertEquals("Минимум 10 символов", viewModel.state.value.descriptionError)
    }

    @Test
    fun `TeamEditViewModel validate long description`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onDescriptionChange("A".repeat(2001))
        viewModel.save {}
        advanceUntilIdle()

        assertEquals("Максимум 2000 символов", viewModel.state.value.descriptionError)
    }

    @Test
    fun `TeamEditViewModel validate invalid meeting room link`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onMeetingRoomLinkChange("not-a-url")
        viewModel.save {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.meetingRoomLinkError)
    }

    @Test
    fun `TeamEditViewModel validate empty meeting room link is ok`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onMeetingRoomLinkChange("")
        viewModel.save {}
        advanceUntilIdle()

        assertNull(viewModel.state.value.meetingRoomLinkError)
    }

    @Test
    fun `TeamEditViewModel save failure sets error`() = runTest {
        val repository = FakeTeamCardRepository(updateResult = Result.failure(Exception("API error")))
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        viewModel.save {}
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSubmitting)
        assertNotNull(viewModel.state.value.submitError)
    }

    @Test
    fun `TeamEditViewModel deactivate failure sets error`() = runTest {
        val repository = FakeTeamCardRepository(deleteResult = Result.failure(Exception("API error")))
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        viewModel.deactivate {}
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isDeleting)
        assertNotNull(viewModel.state.value.deleteError)
    }

    @Test
    fun `TeamEditViewModel deactivate does nothing when originalTeam is null`() = runTest {
        var deleteCalled = false
        val repository = object : FakeTeamCardRepository() {
            override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> {
                deleteCalled = true
                return Result.success(Unit)
            }
        }
        // Create a repository that returns a team with null username
        val nullUsernameRepo = FakeTeamCardRepository(
            teamResult = Result.success(
                TeamCard(
                    id = "team-1", name = "Alpha", meetingRoomLink = "", description = "Desc",
                    status = "ACTIVE", username = "", enabled = true,
                    ntiMarkets = emptyList(), readinessLevel = "5-7", averageGrade = null,
                    stream = null, meetingsCount = 0, meetingsCompletedCount = 0,
                    meetingsNotHappenedCount = 0
                )
            )
        )
        val viewModel = TeamEditViewModel("team-1", nullUsernameRepo, adminHolder())
        advanceUntilIdle()

        // originalTeam.username is empty string, so deactivate should return early
        viewModel.deactivate {}
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isDeleting)
    }

    @Test
    fun `TeamEditViewModel isLinkRegex validates URLs`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        assertTrue(viewModel.isLinkRegex("https://webinar.example.com/room"))
        assertTrue(viewModel.isLinkRegex("http://example.com/path"))
        assertFalse(viewModel.isLinkRegex("not-a-url"))
        assertFalse(viewModel.isLinkRegex(""))
    }

    @Test
    fun `TeamEditViewModel field changes clear errors`() = runTest {
        val viewModel = TeamEditViewModel("team-1", FakeTeamCardRepository(), adminHolder())
        advanceUntilIdle()

        viewModel.onNameChange("")
        viewModel.save {}
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.nameError)

        viewModel.onNameChange("New Name")
        assertNull(viewModel.state.value.nameError)
    }

    @Test
    fun `TeamEditViewModel retry reloads data`() = runTest {
        var callCount = 0
        val repository = object : FakeTeamCardRepository() {
            override suspend fun getTeamById(id: String): Result<TeamCard> {
                callCount++
                return teamResult
            }
        }
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        assertEquals(1, callCount)

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(2, callCount)
    }

    @Test
    fun `TeamEditViewModel loads markets failure sets error`() = runTest {
        val repository = FakeTeamCardRepository(
            teamResult = Result.success(sampleTeamCard()),
            marketsResult = Result.failure(Exception("fail"))
        )
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.loadError)
    }

    @Test
    fun `TeamEditViewModel loads trackers failure sets error`() = runTest {
        val repository = FakeTeamCardRepository(
            teamResult = Result.success(sampleTeamCard()),
            marketsResult = Result.success(listOf(NtiMarket("m1", "market", "Market"))),
            trackersResult = Result.failure(Exception("fail"))
        )
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.loadError)
    }

    @Test
    fun `TeamEditViewModel loads streams failure sets error`() = runTest {
        val repository = FakeTeamCardRepository(
            teamResult = Result.success(sampleTeamCard()),
            marketsResult = Result.success(listOf(NtiMarket("m1", "market", "Market"))),
            trackersResult = Result.success(listOf(TrackerUser("1", "tracker", "Tracker", "t@e.com", null))),
            streamsResult = Result.failure(Exception("fail"))
        )
        val viewModel = TeamEditViewModel("team-1", repository, adminHolder())
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.loadError)
    }

    // ==================== Helpers ====================

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
        id = "team-1", name = "Alpha", meetingRoomLink = "https://link.com",
        description = "Description long enough for validation", status = "ACTIVE",
        username = "tracker", enabled = true,
        ntiMarkets = listOf(NtiMarket("m1", "market", "Market")),
        readinessLevel = "5-7", averageGrade = 4.0,
        stream = Stream("s1", "Stream A", "d", true),
        meetingsCount = 1, meetingsCompletedCount = 1, meetingsNotHappenedCount = 0
    )

    private open class FakeTeamCardRepository(
        val teamResult: Result<TeamCard> = Result.success(
            TeamCard(
                id = "team-1", name = "Alpha", meetingRoomLink = "https://link.com",
                description = "Description long enough for validation", status = "ACTIVE",
                username = "tracker", enabled = true,
                ntiMarkets = listOf(NtiMarket("m1", "market", "Market")),
                readinessLevel = "5-7", averageGrade = 4.0,
                stream = Stream("s1", "Stream A", "d", true),
                meetingsCount = 1, meetingsCompletedCount = 1, meetingsNotHappenedCount = 0
            )
        ),
        private val fullNameResult: Result<String> = Result.success("Tracker"),
        private val streamsResult: Result<List<Stream>> = Result.success(listOf(Stream("s1", "Stream A", "d", true))),
        private val marketsResult: Result<List<NtiMarket>> = Result.success(listOf(NtiMarket("m1", "market", "Market"))),
        private val trackersResult: Result<List<TrackerUser>> = Result.success(
            listOf(TrackerUser("1", "tracker", "Tracker", "t@e.com", null))
        ),
        private val createResult: Result<Unit> = Result.success(Unit),
        private val updateResult: Result<Unit> = Result.success(Unit),
        private val deleteResult: Result<Unit> = Result.success(Unit)
    ) : TeamCardRepository {
        override suspend fun getTeamById(id: String): Result<TeamCard> = teamResult
        override suspend fun getTrackerFullName(username: String): Result<String> = fullNameResult
        override suspend fun getTrackers(): Result<List<TrackerUser>> = trackersResult
        override suspend fun getStreams(): Result<List<Stream>> = streamsResult
        override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = marketsResult
        override suspend fun createTeam(request: CreateTeamRequest): Result<Unit> = createResult
        override suspend fun updateTeam(request: UpdateTeamRequest): Result<Unit> = updateResult
        override suspend fun deleteTeam(teamId: String, trackerUsername: String): Result<Unit> = deleteResult
    }
}