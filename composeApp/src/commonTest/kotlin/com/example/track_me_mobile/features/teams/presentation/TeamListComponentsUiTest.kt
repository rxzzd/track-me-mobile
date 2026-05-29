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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class TeamListComponentsUiTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== TeamListViewModel Tests ====================

    @Test
    fun `TeamListViewModel initialize loads teams`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha", "Stream A", listOf(NtiMarket("m1", "market", "Market")), "3-5"),
            sampleTeamCard("2", "Beta", "Stream B", listOf(NtiMarket("m2", "market2", "Market 2")), "6-8")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        assertIs<TeamListState.Success>(viewModel.state)
        assertEquals(2, (viewModel.state as TeamListState.Success).teams.size)
    }

    @Test
    fun `TeamListViewModel initialize only runs once`() = runTest {
        var callCount = 0
        val repository = object : TeamRepository {
            override suspend fun getTeamCards(
                streamId: String?, page: Int, size: Int
            ): Result<List<TeamCard>> {
                callCount++
                return Result.success(listOf(sampleTeamCard("1", "Alpha")))
            }
        }
        val viewModel = TeamListViewModel(repository)
        viewModel.initialize(null)
        advanceUntilIdle()

        assertEquals(1, callCount)

        viewModel.initialize(null)
        advanceUntilIdle()

        // Should still be 1 since initialize only runs once
        assertEquals(1, callCount)
    }

    @Test
    fun `TeamListViewModel setStreamFilter reloads teams`() = runTest {
        var capturedStreamId: String? = null
        val repository = object : TeamRepository {
            override suspend fun getTeamCards(
                streamId: String?, page: Int, size: Int
            ): Result<List<TeamCard>> {
                capturedStreamId = streamId
                return Result.success(listOf(sampleTeamCard("1", "Alpha")))
            }
        }
        val viewModel = TeamListViewModel(repository)
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.setStreamFilter("stream-2")
        advanceUntilIdle()

        assertEquals("stream-2", capturedStreamId)
    }

    @Test
    fun `TeamListViewModel search filters by team name`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha Team"),
            sampleTeamCard("2", "Beta Team"),
            sampleTeamCard("3", "Gamma Group")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Alpha")
        val state = viewModel.state as TeamListState.Success
        assertEquals(1, state.teams.size)
        assertEquals("Alpha Team", state.teams.first().name)
    }

    @Test
    fun `TeamListViewModel search filters by description`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha", description = "Machine Learning project"),
            sampleTeamCard("2", "Beta", description = "Web Development project")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Machine Learning")
        val state = viewModel.state as TeamListState.Success
        assertEquals(1, state.teams.size)
    }

    @Test
    fun `TeamListViewModel empty search returns all teams`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha"),
            sampleTeamCard("2", "Beta")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Alpha")
        assertEquals(1, (viewModel.state as TeamListState.Success).teams.size)

        viewModel.onSearchQueryChange("")
        assertEquals(2, (viewModel.state as TeamListState.Success).teams.size)
    }

    @Test
    fun `TeamListViewModel case insensitive search`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha Team")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("alpha team")
        assertEquals(1, (viewModel.state as TeamListState.Success).teams.size)

        viewModel.onSearchQueryChange("ALPHA")
        assertEquals(1, (viewModel.state as TeamListState.Success).teams.size)
    }

    @Test
    fun `TeamListViewModel filter by markets`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha", ntiMarkets = listOf(NtiMarket("m1", "market", "Market"))),
            sampleTeamCard("2", "Beta", ntiMarkets = listOf(NtiMarket("m2", "market2", "Market 2")))
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onFilterApply(selectedMarkets = listOf("Market"), selectedTrlRanges = emptyList())
        val state = viewModel.state as TeamListState.Success
        assertEquals(1, state.teams.size)
        assertEquals("Alpha", state.teams.first().name)
    }

    @Test
    fun `TeamListViewModel filter by TRL range`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha", readinessLevel = "3-5"),
            sampleTeamCard("2", "Beta", readinessLevel = "6-8"),
            sampleTeamCard("3", "Gamma", readinessLevel = "9-10")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onFilterApply(selectedMarkets = emptyList(), selectedTrlRanges = listOf(3..5))
        val state = viewModel.state as TeamListState.Success
        assertEquals(1, state.teams.size)
        assertEquals("Alpha", state.teams.first().name)
    }

    @Test
    fun `TeamListViewModel filter by multiple TRL ranges`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha", readinessLevel = "3-5"),
            sampleTeamCard("2", "Beta", readinessLevel = "6-8"),
            sampleTeamCard("3", "Gamma", readinessLevel = "9-10")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onFilterApply(selectedMarkets = emptyList(), selectedTrlRanges = listOf(3..5, 6..8))
        val state = viewModel.state as TeamListState.Success
        assertEquals(2, state.teams.size)
    }

    @Test
    fun `TeamListViewModel filter by both markets and TRL`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha", ntiMarkets = listOf(NtiMarket("m1", "market", "Market")), readinessLevel = "3-5"),
            sampleTeamCard("2", "Beta", ntiMarkets = listOf(NtiMarket("m2", "market2", "Market 2")), readinessLevel = "6-8"),
            sampleTeamCard("3", "Gamma", ntiMarkets = listOf(NtiMarket("m1", "market", "Market")), readinessLevel = "6-8")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onFilterApply(selectedMarkets = listOf("Market"), selectedTrlRanges = listOf(3..5))
        val state = viewModel.state as TeamListState.Success
        assertEquals(1, state.teams.size)
        assertEquals("Alpha", state.teams.first().name)
    }

    @Test
    fun `TeamListViewModel empty filters return all teams`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha"),
            sampleTeamCard("2", "Beta")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onFilterApply(selectedMarkets = emptyList(), selectedTrlRanges = emptyList())
        assertEquals(2, (viewModel.state as TeamListState.Success).teams.size)
    }

    @Test
    fun `TeamListViewModel error state on load failure`() = runTest {
        val viewModel = TeamListViewModel(FakeTeamRepository(getResult = Result.failure(Exception("fail"))))
        viewModel.initialize(null)
        advanceUntilIdle()

        assertIs<TeamListState.Error>(viewModel.state)
    }

    @Test
    fun `TeamListViewModel searchQuery is updated`() = runTest {
        val viewModel = TeamListViewModel(FakeTeamRepository(listOf(sampleTeamCard("1", "Alpha"))))
        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("test query")
        assertEquals("test query", viewModel.searchQuery)
    }

    @Test
    fun `TeamListViewModel pagination loads all pages`() = runTest {
        // pageSize in TeamListViewModel.loadTeams() is 100, so page 0 must return 100 items
        // to trigger a second page load
        val page1 = (1..100).map { sampleTeamCard(it.toString(), "Team$it") }
        val page2 = listOf(sampleTeamCard("101", "Extra"))
        var callCount = 0
        val repository = object : TeamRepository {
            override suspend fun getTeamCards(
                streamId: String?, page: Int, size: Int
            ): Result<List<TeamCard>> {
                callCount++
                return if (page == 0) Result.success(page1) else Result.success(page2)
            }
        }
        val viewModel = TeamListViewModel(repository)
        viewModel.initialize(null)
        advanceUntilIdle()

        assertEquals(2, callCount)
        val state = viewModel.state as TeamListState.Success
        assertEquals(101, state.teams.size)
    }

    @Test
    fun `TeamListViewModel TRL parsing handles non-numeric values`() = runTest {
        val teams = listOf(
            sampleTeamCard("1", "Alpha", readinessLevel = "invalid"),
            sampleTeamCard("2", "Beta", readinessLevel = "3-5")
        )
        val viewModel = TeamListViewModel(FakeTeamRepository(teams))
        viewModel.initialize(null)
        advanceUntilIdle()

        // TRL "invalid" parses to 0, which won't match 3..5
        viewModel.onFilterApply(selectedMarkets = emptyList(), selectedTrlRanges = listOf(3..5))
        val state = viewModel.state as TeamListState.Success
        assertEquals(1, state.teams.size)
        assertEquals("Beta", state.teams.first().name)
    }

    // ==================== Helpers ====================

    private fun sampleTeamCard(
        id: String = "1",
        name: String = "Team",
        streamName: String? = null,
        ntiMarkets: List<NtiMarket> = emptyList(),
        readinessLevel: String = "3-5",
        description: String = "Description"
    ) = TeamCard(
        id = id,
        name = name,
        meetingRoomLink = "",
        description = description,
        status = "ACTIVE",
        username = "tracker",
        enabled = true,
        ntiMarkets = ntiMarkets,
        readinessLevel = readinessLevel,
        averageGrade = null,
        stream = if (streamName != null) Stream("s-$id", streamName, "d", true) else null,
        meetingsCount = 0,
        meetingsCompletedCount = 0,
        meetingsNotHappenedCount = 0
    )

    private class FakeTeamRepository(
        private val teams: List<TeamCard> = emptyList(),
        private val getResult: Result<List<TeamCard>>? = null
    ) : TeamRepository {
        override suspend fun getTeamCards(
            streamId: String?, page: Int, size: Int
        ): Result<List<TeamCard>> {
            return getResult ?: Result.success(teams)
        }
    }
}