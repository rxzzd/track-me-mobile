package com.example.track_me_mobile.features.streams.presentation

import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import com.example.track_me_mobile.features.streams.domain.models.Stream
import com.example.track_me_mobile.features.streams.domain.models.StreamFilter
import com.example.track_me_mobile.features.streams.domain.StreamPage
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
class StreamListViewModelBigTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── StreamListViewModel: init ────────────────────────────────────────────

    @Test
    fun `StreamListViewModel init loads streams and markets`() = runTest {
        val streams = listOf(sampleStream("s1", "Stream 1"))
        val markets = listOf(NtiMarket("market-1", "Market 1", "M1"))
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(streams, 0, 1, 1)),
            getMarketsResult = Result.success(markets)
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.isLoading)
        assertNull(vm.errorMessage)
        assertEquals(1, vm.streams.size)
        assertEquals("Stream 1", vm.streams.first().name)
        assertEquals(1, vm.availableMarkets.size)
        assertEquals("M1", vm.availableMarkets.first())
    }

    @Test
    fun `StreamListViewModel init with empty streams`() = runTest {
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(emptyList(), 0, 0, 0)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        assertTrue(vm.streams.isEmpty())
        assertTrue(vm.availableMarkets.isEmpty())
    }

    @Test
    fun `StreamListViewModel init failure sets error`() = runTest {
        val repo = FakeStreamRepository(
            getStreamsResult = Result.failure(Exception("network error")),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        assertEquals("Не удалось загрузить потоки", vm.errorMessage)
        assertFalse(vm.isLoading)
    }

    @Test
    fun `StreamListViewModel init with multiple pages`() = runTest {
        val page0 = (1..1000).map { sampleStream("s$it", "Stream $it") }
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(page0, 2, 1000, 0)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        assertEquals(1000, vm.streams.size)
        assertTrue(vm.hasMore)
    }

    // ─── StreamListViewModel: search ──────────────────────────────────────────

    @Test
    fun `onSearchQueryChange filters streams locally`() = runTest {
        val streams = listOf(
            sampleStream("s1", "Alpha"),
            sampleStream("s2", "Beta"),
            sampleStream("s3", "Alpha Prime")
        )
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(streams, 0, 3, 1)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChange("Alpha")
        assertEquals(2, vm.streams.size)
        assertTrue(vm.streams.all { it.name.contains("Alpha", ignoreCase = true) })
    }

    @Test
    fun `onSearchQueryChange with empty query shows all`() = runTest {
        val streams = listOf(
            sampleStream("s1", "Alpha"),
            sampleStream("s2", "Beta")
        )
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(streams, 0, 2, 1)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChange("Alpha")
        assertEquals(1, vm.streams.size)

        vm.onSearchQueryChange("")
        assertEquals(2, vm.streams.size)
    }

    @Test
    fun `onSearchQueryChange with no match shows empty`() = runTest {
        val streams = listOf(
            sampleStream("s1", "Alpha"),
            sampleStream("s2", "Beta")
        )
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(streams, 0, 2, 1)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChange("NonExistent")
        assertTrue(vm.streams.isEmpty())
    }

    @Test
    fun `onSearchQueryChange is case insensitive`() = runTest {
        val streams = listOf(
            sampleStream("s1", "Alpha Stream"),
            sampleStream("s2", "Beta Stream")
        )
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(streams, 0, 2, 1)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChange("alpha")
        assertEquals(1, vm.streams.size)
        assertEquals("Alpha Stream", vm.streams.first().name)
    }

    // ─── StreamListViewModel: filters ─────────────────────────────────────────

    @Test
    fun `onYearToggle adds and removes year`() = runTest {
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(emptyList(), 0, 0, 0)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onYearToggle("2024")
        assertTrue("2024" in vm.selectedYears)

        vm.onYearToggle("2024")
        assertFalse("2024" in vm.selectedYears)
    }

    @Test
    fun `onYearToggle multiple years`() = runTest {
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(emptyList(), 0, 0, 0)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onYearToggle("2024")
        vm.onYearToggle("2025")
        assertEquals(setOf("2024", "2025"), vm.selectedYears)
    }

    @Test
    fun `onMarketToggle adds and removes market`() = runTest {
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(emptyList(), 0, 0, 0)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onMarketToggle("Market A")
        assertTrue("Market A" in vm.selectedMarkets)

        vm.onMarketToggle("Market A")
        assertFalse("Market A" in vm.selectedMarkets)
    }

    @Test
    fun `onTrlToggle adds and removes TRL`() = runTest {
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(emptyList(), 0, 0, 0)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onTrlToggle("5")
        assertTrue("5" in vm.selectedTrls)

        vm.onTrlToggle("5")
        assertFalse("5" in vm.selectedTrls)
    }

    @Test
    fun `onYearsChange sets years`() = runTest {
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(emptyList(), 0, 0, 0)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onYearsChange(setOf("2024", "2025"))
        assertEquals(setOf("2024", "2025"), vm.selectedYears)
    }

    @Test
    fun `onMarketsChange sets markets`() = runTest {
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(emptyList(), 0, 0, 0)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onMarketsChange(setOf("M1", "M2"))
        assertEquals(setOf("M1", "M2"), vm.selectedMarkets)
    }

    @Test
    fun `onTrlsChange sets TRLs`() = runTest {
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(emptyList(), 0, 0, 0)),
            getMarketsResult = Result.success(emptyList())
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onTrlsChange(setOf("3", "5"))
        assertEquals(setOf("3", "5"), vm.selectedTrls)
    }

    @Test
    fun `resetFilters clears all filters and reloads`() = runTest {
        var loadCount = 0
        val repo = object : StreamRepository {
            override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> {
                loadCount++
                return Result.success(StreamPage(emptyList(), 0, 0, 0))
            }
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun getStream(id: String): Result<Stream> = Result.failure(Exception("n/a"))
            override suspend fun getStreamImage(id: String): Result<ByteArray?> = Result.failure(Exception("n/a"))
            override suspend fun createStream(request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun updateStream(id: String, request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun deleteStream(id: String): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun getTeamsByStream(streamId: String): Result<List<com.example.track_me_mobile.features.teams.domain.models.TeamCard>> =
                Result.failure(Exception("n/a"))
        }
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChange("test")
        vm.onYearToggle("2024")
        vm.onMarketToggle("M1")
        vm.onTrlToggle("5")

        vm.resetFilters()
        advanceUntilIdle()

        assertEquals("", vm.searchQuery)
        assertTrue(vm.selectedYears.isEmpty())
        assertTrue(vm.selectedMarkets.isEmpty())
        assertTrue(vm.selectedTrls.isEmpty())
        assertEquals(2, loadCount) // init + reset
    }

    // ─── StreamListViewModel: pagination ──────────────────────────────────────

    @Test
    fun `loadNextPage loads more streams`() = runTest {
        var page = 0
        val repo = object : StreamRepository {
            override suspend fun getStreams(filters: List<StreamFilter>, pageIn: Int, size: Int): Result<StreamPage> {
                page = pageIn
                return if (pageIn == 0) {
                    Result.success(StreamPage(listOf(sampleStream("s1", "S1")), 2, 1, 0))
                } else {
                    Result.success(StreamPage(listOf(sampleStream("s2", "S2")), 2, 2, 1))
                }
            }
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun getStream(id: String): Result<Stream> = Result.failure(Exception("n/a"))
            override suspend fun getStreamImage(id: String): Result<ByteArray?> = Result.failure(Exception("n/a"))
            override suspend fun createStream(request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun updateStream(id: String, request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun deleteStream(id: String): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun getTeamsByStream(streamId: String): Result<List<com.example.track_me_mobile.features.teams.domain.models.TeamCard>> =
                Result.failure(Exception("n/a"))
        }
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        assertEquals(1, vm.streams.size)
        assertTrue(vm.hasMore)

        vm.loadNextPage()
        advanceUntilIdle()

        assertEquals(2, vm.streams.size)
    }

    @Test
    fun `loadNextPage does nothing when hasMore is false`() = runTest {
        var callCount = 0
        val repo = object : StreamRepository {
            override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> {
                callCount++
                return Result.success(StreamPage(listOf(sampleStream("s1", "S1")), 0, 1, 1))
            }
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun getStream(id: String): Result<Stream> = Result.failure(Exception("n/a"))
            override suspend fun getStreamImage(id: String): Result<ByteArray?> = Result.failure(Exception("n/a"))
            override suspend fun createStream(request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun updateStream(id: String, request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun deleteStream(id: String): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun getTeamsByStream(streamId: String): Result<List<com.example.track_me_mobile.features.teams.domain.models.TeamCard>> =
                Result.failure(Exception("n/a"))
        }
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.hasMore)
        assertEquals(1, callCount)

        vm.loadNextPage()
        advanceUntilIdle()

        assertEquals(1, callCount) // should not call again
    }

    @Test
    fun `loadNextPage does nothing when isLoadingMore`() = runTest {
        var callCount = 0
        val repo = object : StreamRepository {
            override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> {
                callCount++
                return Result.success(StreamPage(listOf(sampleStream("s1", "S1")), 2, 1, 0))
            }
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun getStream(id: String): Result<Stream> = Result.failure(Exception("n/a"))
            override suspend fun getStreamImage(id: String): Result<ByteArray?> = Result.failure(Exception("n/a"))
            override suspend fun createStream(request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun updateStream(id: String, request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun deleteStream(id: String): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun getTeamsByStream(streamId: String): Result<List<com.example.track_me_mobile.features.teams.domain.models.TeamCard>> =
                Result.failure(Exception("n/a"))
        }
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        // hasMore is true, so loadNextPage should call getStreams again
        vm.loadNextPage()
        advanceUntilIdle()

        assertEquals(2, callCount)
    }

    // ─── StreamListViewModel: applyFilters ────────────────────────────────────

    @Test
    fun `applyFilters reloads streams with filters`() = runTest {
        var capturedFilters: List<StreamFilter> = emptyList()
        val repo = object : StreamRepository {
            override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> {
                capturedFilters = filters
                return Result.success(StreamPage(emptyList(), 0, 0, 0))
            }
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun getStream(id: String): Result<Stream> = Result.failure(Exception("n/a"))
            override suspend fun getStreamImage(id: String): Result<ByteArray?> = Result.failure(Exception("n/a"))
            override suspend fun createStream(request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun updateStream(id: String, request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun deleteStream(id: String): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun getTeamsByStream(streamId: String): Result<List<com.example.track_me_mobile.features.teams.domain.models.TeamCard>> =
                Result.failure(Exception("n/a"))
        }
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        vm.onYearToggle("2024")
        vm.onMarketToggle("Market 1")
        vm.onTrlToggle("5")
        vm.applyFilters()
        advanceUntilIdle()

        assertTrue(capturedFilters.isNotEmpty())
    }

    // ─── StreamListViewModel: markets ─────────────────────────────────────────

    @Test
    fun `loadMarkets populates availableMarkets`() = runTest {
        val markets = listOf(
            NtiMarket("m1", "Market A", "market_a"),
            NtiMarket("m2", "Market B", "market_b")
        )
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(emptyList(), 0, 0, 0)),
            getMarketsResult = Result.success(markets)
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        assertEquals(2, vm.availableMarkets.size)
        assertTrue("market_a" in vm.availableMarkets)
        assertTrue("market_b" in vm.availableMarkets)
    }

    @Test
    fun `loadMarkets failure does not crash`() = runTest {
        val repo = FakeStreamRepository(
            getStreamsResult = Result.success(StreamPage(emptyList(), 0, 0, 0)),
            getMarketsResult = Result.failure(Exception("fail"))
        )
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        assertTrue(vm.availableMarkets.isEmpty())
    }

    // ─── StreamListViewModel: stream images ───────────────────────────────────

    @Test
    fun `stream images are loaded after streams`() = runTest {
        var imageLoaded = false
        val repo = object : StreamRepository {
            override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> {
                return Result.success(StreamPage(listOf(sampleStream("s1", "S1")), 0, 1, 1))
            }
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun getStream(id: String): Result<Stream> = Result.failure(Exception("n/a"))
            override suspend fun getStreamImage(id: String): Result<ByteArray?> {
                imageLoaded = true
                return Result.success(byteArrayOf(1, 2, 3))
            }
            override suspend fun createStream(request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun updateStream(id: String, request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun deleteStream(id: String): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun getTeamsByStream(streamId: String): Result<List<com.example.track_me_mobile.features.teams.domain.models.TeamCard>> =
                Result.failure(Exception("n/a"))
        }
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        assertTrue(imageLoaded)
        assertNotNull(vm.streamImages["s1"])
    }

    @Test
    fun `stream image failure sets null`() = runTest {
        val repo = object : StreamRepository {
            override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> {
                return Result.success(StreamPage(listOf(sampleStream("s1", "S1")), 0, 1, 1))
            }
            override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(emptyList())
            override suspend fun getStream(id: String): Result<Stream> = Result.failure(Exception("n/a"))
            override suspend fun getStreamImage(id: String): Result<ByteArray?> {
                return Result.failure(Exception("not found"))
            }
            override suspend fun createStream(request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun updateStream(id: String, request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
                Result.failure(Exception("n/a"))
            override suspend fun deleteStream(id: String): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.failure(Exception("n/a"))
            override suspend fun getTeamsByStream(streamId: String): Result<List<com.example.track_me_mobile.features.teams.domain.models.TeamCard>> =
                Result.failure(Exception("n/a"))
        }
        val vm = StreamListViewModel(repo)
        advanceUntilIdle()

        assertNull(vm.streamImages["s1"])
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun sampleStream(id: String, name: String) = Stream(
        id = id,
        name = name,
        startDate = "2024-01-01",
        endDate = "2024-12-31",
        description = "Description of $name",
        active = true,
        trackStartDate = "2024-01-15",
        meetingsCount = 10,
        ntiMarkets = emptyList()
    )

    private open class FakeStreamRepository(
        private val getStreamsResult: Result<StreamPage> = Result.failure(Exception("not configured")),
        private val getMarketsResult: Result<List<NtiMarket>> = Result.failure(Exception("not configured"))
    ) : StreamRepository {
        override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> = getStreamsResult
        override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = getMarketsResult
        override suspend fun getStream(id: String): Result<Stream> = Result.failure(Exception("n/a"))
        override suspend fun getStreamImage(id: String): Result<ByteArray?> = Result.failure(Exception("n/a"))
        override suspend fun createStream(request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
            Result.failure(Exception("n/a"))
        override suspend fun updateStream(id: String, request: com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest): Result<com.example.track_me_mobile.features.streams.domain.models.Stream> =
            Result.failure(Exception("n/a"))
        override suspend fun deleteStream(id: String): Result<Unit> = Result.failure(Exception("n/a"))
        override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.failure(Exception("n/a"))
        override suspend fun getTeamsByStream(streamId: String): Result<List<com.example.track_me_mobile.features.teams.domain.models.TeamCard>> =
            Result.failure(Exception("n/a"))
    }
}