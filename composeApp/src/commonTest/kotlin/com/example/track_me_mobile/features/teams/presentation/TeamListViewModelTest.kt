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
class TeamListViewModelTest {

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
    fun `initialize loads teams and sets success state`() = runTest {
        val repository = FakeTeamRepository(
            teamCards = listOf(
                TeamCard(
                    id = "team-1",
                    name = "Alpha Team",
                    meetingRoomLink = "link",
                    description = "Description",
                    status = "ACTIVE",
                    username = "alpha",
                    enabled = true,
                    ntiMarkets = listOf(NtiMarket(id = "1", name = "Market", displayName = "Market")),
                    readinessLevel = "5 - Начальная",
                    averageGrade = 4.5,
                    stream = Stream(id = "s1", name = "Stream A", description = "Desc", active = true),
                    meetingsCount = 1,
                    meetingsCompletedCount = 1,
                    meetingsNotHappenedCount = 0
                )
            )
        )
        val viewModel = TeamListViewModel(repository)

        viewModel.initialize(null)
        advanceUntilIdle()

        assertTrue(viewModel.state is TeamListState.Success)
        assertEquals(1, (viewModel.state as TeamListState.Success).teams.size)
    }

    @Test
    fun `onSearchQueryChange filters teams by name`() = runTest {
        val repository = FakeTeamRepository(
            teamCards = listOf(
                TeamCard(
                    id = "team-1",
                    name = "Alpha Team",
                    meetingRoomLink = "link",
                    description = "Description",
                    status = "ACTIVE",
                    username = "alpha",
                    enabled = true,
                    ntiMarkets = listOf(NtiMarket(id = "1", name = "Market", displayName = "Market")),
                    readinessLevel = "5 - Начальная",
                    averageGrade = 4.5,
                    stream = Stream(id = "s1", name = "Stream A", description = "Desc", active = true),
                    meetingsCount = 1,
                    meetingsCompletedCount = 1,
                    meetingsNotHappenedCount = 0
                ),
                TeamCard(
                    id = "team-2",
                    name = "Beta Team",
                    meetingRoomLink = "link",
                    description = "Another description",
                    status = "ACTIVE",
                    username = "beta",
                    enabled = true,
                    ntiMarkets = listOf(NtiMarket(id = "2", name = "Market2", displayName = "Market2")),
                    readinessLevel = "4 - Средняя",
                    averageGrade = 3.5,
                    stream = Stream(id = "s2", name = "Stream B", description = "Desc", active = true),
                    meetingsCount = 1,
                    meetingsCompletedCount = 1,
                    meetingsNotHappenedCount = 0
                )
            )
        )
        val viewModel = TeamListViewModel(repository)

        viewModel.initialize(null)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Beta")

        assertTrue(viewModel.state is TeamListState.Success)
        assertEquals(1, (viewModel.state as TeamListState.Success).teams.size)
        assertEquals("Beta Team", (viewModel.state as TeamListState.Success).teams.first().name)
    }

    private class FakeTeamRepository(
        private val teamCards: List<TeamCard>
    ) : TeamRepository {
        override suspend fun getTeamCards(streamId: String?, page: Int, size: Int): Result<List<TeamCard>> {
            return Result.success(teamCards)
        }
    }
}
