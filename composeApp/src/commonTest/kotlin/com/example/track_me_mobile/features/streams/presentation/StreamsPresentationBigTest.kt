package com.example.track_me_mobile.features.streams.presentation

import com.example.track_me_mobile.features.streams.data.StreamHasTeamsException
import com.example.track_me_mobile.features.streams.domain.StreamPage
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import com.example.track_me_mobile.features.streams.domain.models.Stream
import com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest
import com.example.track_me_mobile.features.streams.domain.models.StreamFilter
import com.example.track_me_mobile.features.teams.domain.models.NtiMarket as TeamNtiMarket
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StreamsPresentationBigTest {

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
    fun `StreamListViewModel loads streams on init`() = runTest {
        val streams = listOf(sampleStream("s1", "Alpha Stream"), sampleStream("s2", "Beta Stream"))
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = streams))
        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertEquals(2, viewModel.streams.size)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `StreamListViewModel search filters streams locally`() = runTest {
        val viewModel = StreamListViewModel(
            FakeStreamRepository(streams = listOf(sampleStream("1", "Alpha"), sampleStream("2", "Beta")))
        )
        advanceUntilIdle()

        viewModel.onSearchQueryChange("beta")
        assertEquals(1, viewModel.streams.size)
        assertEquals("Beta", viewModel.streams.first().name)
    }

    @Test
    fun `StreamListViewModel toggle filters and applyFilters reloads`() = runTest {
        var lastFilters: List<StreamFilter> = emptyList()
        val repository = object : FakeStreamRepository() {
            override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> {
                lastFilters = filters
                return Result.success(StreamPage(emptyList(), 1, 0, 0))
            }
        }
        val viewModel = StreamListViewModel(repository)
        advanceUntilIdle()

        viewModel.onYearToggle("2024")
        viewModel.onMarketToggle("Market A")
        viewModel.onTrlToggle("5-7")
        viewModel.applyFilters()
        advanceUntilIdle()

        assertTrue(lastFilters.any { it.fieldName == "year" && it.value == "2024" })
        assertTrue(lastFilters.any { it.fieldName == "ntiMarkets.name" })
        assertTrue(lastFilters.any { it.fieldName == "teamCards.readinessLevel" })
    }

    @Test
    fun `StreamListViewModel resetFilters clears all filters`() = runTest {
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = listOf(sampleStream("1", "A"))))
        advanceUntilIdle()

        viewModel.onSearchQueryChange("test")
        viewModel.onYearToggle("2024")
        viewModel.resetFilters()
        advanceUntilIdle()

        assertEquals("", viewModel.searchQuery)
        assertTrue(viewModel.selectedYears.isEmpty())
    }

    @Test
    fun `StreamListViewModel load failure sets error message`() = runTest {
        val viewModel = StreamListViewModel(
            FakeStreamRepository(getStreamsResult = Result.failure(Exception("fail")))
        )
        advanceUntilIdle()

        assertEquals("Не удалось загрузить потоки", viewModel.errorMessage)
    }

    @Test
    fun `StreamListViewModel loadNextPage appends streams`() = runTest {
        var page = 0
        val repository = object : FakeStreamRepository() {
            override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> {
                return if (page == 0) {
                    Result.success(StreamPage(listOf(sampleStream("1", "Page1")), totalPages = 2, totalElements = 2, currentPage = 0))
                } else {
                    Result.success(StreamPage(listOf(sampleStream("2", "Page2")), totalPages = 2, totalElements = 2, currentPage = 1))
                }
            }
        }
        val viewModel = StreamListViewModel(repository)
        advanceUntilIdle()
        assertEquals(1, viewModel.streams.size)

        viewModel.loadNextPage()
        advanceUntilIdle()
        assertEquals(2, viewModel.streams.size)
    }

    @Test
    fun `StreamListViewModel loads markets and maps display names`() = runTest {
        val viewModel = StreamListViewModel(
            FakeStreamRepository(
                markets = listOf(NtiMarket("m1", "internal", "Display Name"))
            )
        )
        advanceUntilIdle()

        assertTrue(viewModel.availableMarkets.contains("Display Name"))
    }

    @Test
    fun `AddStreamViewModel loads markets on init`() = runTest {
        val viewModel = AddStreamViewModel(
            FakeStreamRepository(markets = listOf(NtiMarket("m1", "n", "Market")))
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.availableMarkets.size)
    }

    @Test
    fun `AddStreamViewModel createStream validates required fields`() = runTest {
        val viewModel = AddStreamViewModel(FakeStreamRepository())
        advanceUntilIdle()

        viewModel.createStream {}
        assertEquals("Заполните обязательные поля", viewModel.errorMessage)

        viewModel.name = "Stream"
        viewModel.startDate = "2024-01-01"
        viewModel.endDate = "2024-12-31"
        viewModel.trackStartDate = "2024-02-01"
        viewModel.createStream {}
        assertEquals("Выберите количество встреч", viewModel.errorMessage)
    }

    @Test
    fun `AddStreamViewModel createStream success without image`() = runTest {
        val viewModel = AddStreamViewModel(FakeStreamRepository())
        advanceUntilIdle()

        viewModel.name = "New Stream"
        viewModel.startDate = "2024-01-01"
        viewModel.endDate = "2024-12-31"
        viewModel.trackStartDate = "2024-02-01"
        viewModel.meetingsCount = 5
        var success = false
        viewModel.createStream { success = true }
        advanceUntilIdle()

        assertTrue(success)
        assertTrue(viewModel.isSuccess)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `AddStreamViewModel createStream uploads image when pending`() = runTest {
        var uploaded = false
        val repository = object : FakeStreamRepository() {
            override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> {
                uploaded = true
                return Result.success(Unit)
            }
        }
        val viewModel = AddStreamViewModel(repository)
        advanceUntilIdle()

        viewModel.name = "Stream"
        viewModel.startDate = "2024-01-01"
        viewModel.endDate = "2024-12-31"
        viewModel.trackStartDate = "2024-02-01"
        viewModel.meetingsCount = 3
        viewModel.setStreamImage(byteArrayOf(1, 2))
        viewModel.createStream {}
        advanceUntilIdle()

        assertTrue(uploaded)
        assertTrue(viewModel.isSuccess)
    }

    @Test
    fun `AddStreamViewModel toggleMarket adds and removes ids`() = runTest {
        val viewModel = AddStreamViewModel(FakeStreamRepository())
        viewModel.toggleMarket("m1")
        assertTrue(viewModel.selectedMarketIds.contains("m1"))
        viewModel.toggleMarket("m1")
        assertFalse(viewModel.selectedMarketIds.contains("m1"))
    }

    @Test
    fun `EditStreamViewModel loads stream fields`() = runTest {
        val stream = sampleStream("s1", "Edited Stream")
        val viewModel = EditStreamViewModel(FakeStreamRepository(stream = stream), "s1")
        advanceUntilIdle()

        assertEquals("Edited Stream", viewModel.name)
        assertEquals(stream.startDate, viewModel.startDate)
        assertEquals(stream.meetingsCount, viewModel.meetingsCount)
    }

    @Test
    fun `EditStreamViewModel updateStream validates required fields`() = runTest {
        val viewModel = EditStreamViewModel(FakeStreamRepository(stream = sampleStream("s1", "S")), "s1")
        advanceUntilIdle()

        viewModel.name = ""
        viewModel.updateStream {}
        assertEquals("Заполните обязательные поля", viewModel.errorMessage)
    }

    @Test
    fun `EditStreamViewModel updateStream maps trackStartDate error`() = runTest {
        val repository = object : FakeStreamRepository(stream = sampleStream("s1", "S")) {
            override suspend fun updateStream(id: String, request: StreamCreateRequest): Result<Stream> {
                return Result.failure(Exception("trackStartDate: Дата начала трека должна быть в будущем"))
            }
        }
        val viewModel = EditStreamViewModel(repository, "s1")
        advanceUntilIdle()

        viewModel.updateStream {}
        advanceUntilIdle()

        assertEquals("Дата начала трека должна быть в будущем", viewModel.errorMessage)
    }

    @Test
    fun `EditStreamViewModel deleteStream shows teams conflict dialog`() = runTest {
        val teams = listOf(
            TeamCard(
                id = "t1",
                name = "Team",
                meetingRoomLink = "l",
                description = "d",
                status = "ACTIVE",
                username = "u",
                enabled = true,
                ntiMarkets = emptyList(),
                readinessLevel = "5",
                averageGrade = null,
                stream = null,
                meetingsCount = 0,
                meetingsCompletedCount = 0,
                meetingsNotHappenedCount = 0
            )
        )
        val repository = object : FakeStreamRepository() {
            override suspend fun deleteStream(streamId: String): Result<Unit> {
                return Result.failure(StreamHasTeamsException("teams"))
            }

            override suspend fun getTeamsByStream(streamId: String): Result<List<TeamCard>> {
                return Result.success(teams)
            }
        }
        val viewModel = EditStreamViewModel(repository, "s1")
        advanceUntilIdle()

        viewModel.deleteStream {}
        advanceUntilIdle()

        assertTrue(viewModel.showTeamsConflictDialog)
        assertEquals(1, viewModel.teamsInStream.size)
        viewModel.dismissTeamsDialog()
        assertFalse(viewModel.showTeamsConflictDialog)
    }

    @Test
    fun `EditStreamViewModel uploadStreamImage updates bytes`() = runTest {
        val viewModel = EditStreamViewModel(FakeStreamRepository(stream = sampleStream("s1", "S")), "s1")
        advanceUntilIdle()

        viewModel.setStreamImage(byteArrayOf(1, 2, 3))
        viewModel.uploadStreamImage()
        advanceUntilIdle()

        assertEquals(3, viewModel.streamImageBytes?.size)
        assertNull(viewModel.pendingImageBytes)
    }

    private fun sampleStream(id: String, name: String) = Stream(
        id = id,
        name = name,
        startDate = "2024-01-01",
        endDate = "2024-12-31",
        description = "Description",
        active = true,
        trackStartDate = "2024-02-01",
        meetingsCount = 5,
        ntiMarkets = listOf(NtiMarket("m1", "market-a", "Market A"))
    )

    private open class FakeStreamRepository(
        private val streams: List<Stream> = emptyList(),
        private val markets: List<NtiMarket> = listOf(NtiMarket("m1", "market-a", "Market A")),
        private val stream: Stream? = null,
        private val getStreamsResult: Result<StreamPage>? = null
    ) : StreamRepository {
        override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> {
            return getStreamsResult ?: Result.success(
                StreamPage(
                    content = streams,
                    totalPages = 1,
                    totalElements = streams.size.toLong(),
                    currentPage = page
                )
            )
        }

        override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(markets)

        override suspend fun createStream(request: StreamCreateRequest): Result<Stream> {
            return Result.success(
                Stream(
                    id = "created",
                    name = request.name,
                    startDate = request.startDate,
                    endDate = request.endDate,
                    description = request.description,
                    active = true,
                    trackStartDate = request.trackStartDate,
                    meetingsCount = request.meetingsCount,
                    ntiMarkets = emptyList()
                )
            )
        }

        override suspend fun getStream(id: String): Result<Stream> {
            return stream?.let { Result.success(it) } ?: Result.failure(Exception("not found"))
        }

        override suspend fun updateStream(id: String, request: StreamCreateRequest): Result<Stream> {
            return Result.success(
                Stream(
                    id = id,
                    name = request.name,
                    startDate = request.startDate,
                    endDate = request.endDate,
                    description = request.description,
                    active = true,
                    trackStartDate = request.trackStartDate,
                    meetingsCount = request.meetingsCount,
                    ntiMarkets = emptyList()
                )
            )
        }

        override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.success(Unit)

        override suspend fun getStreamImage(streamId: String): Result<ByteArray?> = Result.success(null)

        override suspend fun deleteStream(streamId: String): Result<Unit> = Result.success(Unit)

        override suspend fun getTeamsByStream(streamId: String): Result<List<TeamCard>> = Result.success(emptyList())
    }
}
