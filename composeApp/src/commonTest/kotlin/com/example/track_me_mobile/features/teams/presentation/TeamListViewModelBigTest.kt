package com.example.track_me_mobile.features.teams.presentation

import com.example.track_me_mobile.features.teams.domain.TeamRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TeamListViewModelBigTest {

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
    fun `initialize is idempotent and loads once`() = runTest {
        var calls = 0
        val repository = object : TeamRepository {
            override suspend fun getTeamCards(streamId: String?, page: Int, size: Int): Result<List<TeamCard>> {
                calls++
                return Result.success(listOf(sampleTeam("Alpha")))
            }
        }
        val viewModel = TeamListViewModel(repository)
        viewModel.initialize(null)
        viewModel.initialize(null)
        advanceUntilIdle()

        assertEquals(1, calls)
    }

    @Test
    fun `loadTeams paginates until page smaller than pageSize`() = runTest {
        val page0 = (1..100).map { sampleTeam("Team $it") }
        val page1 = listOf(sampleTeam("Team 101"))
        var page = 0
        val repository = object : TeamRepository {
            override suspend fun getTeamCards(streamId: String?, page: Int, size: Int): Result<List<TeamCard>> {
                return Result.success(if (page == 0) page0 else page1)
            }
        }
        val viewModel = TeamListViewModel(repository)
        viewModel.initialize(null)
        advanceUntilIdle()

        assertTrue(viewModel.state is TeamListState.Success)
        assertEquals(101, (viewModel.state as TeamListState.Success).teams.size)
    }

    @Test
    fun `loadTeams error sets error state`() = runTest {
        val repository = object : TeamRepository {
            override suspend fun getTeamCards(streamId: String?, page: Int, size: Int): Result<List<TeamCard>> {
                return Result.failure(Exception("network"))
            }
        }
        val viewModel = TeamListViewModel(repository)
        viewModel.initialize(null)
        advanceUntilIdle()

        assertTrue(viewModel.state is TeamListState.Error)
    }

    @Test
    fun `onFilterApply filters by market and TRL range`() = runTest {
        val teams = listOf(
            sampleTeam("Alpha", market = "Market A", trl = "5 - Начальная"),
            sampleTeam("Beta", market = "Market B", trl = "3 - Средняя")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onFilterApply(selectedMarkets = listOf("Market B"), selectedTrlRanges = listOf(3..5))
        assertEquals(1, (viewModel.state as TeamListState.Success).teams.size)
        assertEquals("Beta", (viewModel.state as TeamListState.Success).teams.first().name)
    }

    @Test
    fun `setStreamFilter reloads teams with stream filter`() = runTest {
        var lastStreamId: String? = null
        val repository = object : TeamRepository {
            override suspend fun getTeamCards(streamId: String?, page: Int, size: Int): Result<List<TeamCard>> {
                lastStreamId = streamId
                return Result.success(emptyList())
            }
        }
        val viewModel = TeamListViewModel(repository)
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.setStreamFilter("Stream X")
        advanceUntilIdle()

        assertEquals("Stream X", lastStreamId)
    }

    @Test
    fun `onSearchQueryChange filters by description`() = runTest {
        val teams = listOf(
            sampleTeam("Alpha", description = "Rocket science project"),
            sampleTeam("Beta", description = "Food delivery app")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("food")
        assertEquals(1, (viewModel.state as TeamListState.Success).teams.size)
        assertEquals("Beta", (viewModel.state as TeamListState.Success).teams.first().name)
    }

    private fun sampleTeam(
        name: String,
        market: String = "Market",
        trl: String = "5 - Начальная",
        description: String = "Description"
    ) = TeamCard(
        id = name,
        name = name,
        meetingRoomLink = "link",
        description = description,
        status = "ACTIVE",
        username = "user",
        enabled = true,
        ntiMarkets = listOf(NtiMarket("1", market, market)),
        readinessLevel = trl,
        averageGrade = 4.0,
        stream = Stream("s1", "Stream", "d", true),
        meetingsCount = 1,
        meetingsCompletedCount = 1,
        meetingsNotHappenedCount = 0
    )

    private class FakeTeamRepository(
        private val teamCards: List<TeamCard>
    ) : TeamRepository {
        override suspend fun getTeamCards(streamId: String?, page: Int, size: Int): Result<List<TeamCard>> {
            return Result.success(teamCards)
        }
    }
}
