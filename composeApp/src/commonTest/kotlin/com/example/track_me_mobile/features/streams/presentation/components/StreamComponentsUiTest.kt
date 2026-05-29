package com.example.track_me_mobile.features.streams.presentation.components

import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import com.example.track_me_mobile.features.streams.domain.models.Stream
import com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest
import com.example.track_me_mobile.features.streams.domain.models.StreamFilter
import com.example.track_me_mobile.features.streams.domain.StreamPage
import com.example.track_me_mobile.features.streams.presentation.StreamListViewModel
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
class StreamComponentsUiTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== StreamListViewModel Tests ====================

    @Test
    fun `StreamListViewModel loads streams on init`() = runTest {
        val streams = listOf(
            sampleStream(id = "s1", name = "Stream A"),
            sampleStream(id = "s2", name = "Stream B")
        )
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = streams))
        advanceUntilIdle()

        assertEquals(2, viewModel.streams.size)
        assertEquals("Stream A", viewModel.streams[0].name)
        assertEquals("Stream B", viewModel.streams[1].name)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `StreamListViewModel search filters streams by name`() = runTest {
        val streams = listOf(
            sampleStream(id = "s1", name = "Alpha"),
            sampleStream(id = "s2", name = "Beta"),
            sampleStream(id = "s3", name = "Alpha Prime")
        )
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = streams))
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Alpha")
        assertEquals(2, viewModel.streams.size)
        assertTrue(viewModel.streams.all { it.name.contains("Alpha", ignoreCase = true) })
    }

    @Test
    fun `StreamListViewModel search with empty query returns all streams`() = runTest {
        val streams = listOf(
            sampleStream(id = "s1", name = "Alpha"),
            sampleStream(id = "s2", name = "Beta")
        )
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = streams))
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Alpha")
        assertEquals(1, viewModel.streams.size)

        viewModel.onSearchQueryChange("")
        assertEquals(2, viewModel.streams.size)
    }

    @Test
    fun `StreamListViewModel search is case insensitive`() = runTest {
        val streams = listOf(
            sampleStream(id = "s1", name = "Alpha Stream"),
            sampleStream(id = "s2", name = "beta stream")
        )
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = streams))
        advanceUntilIdle()

        viewModel.onSearchQueryChange("ALPHA")
        assertEquals(1, viewModel.streams.size)

        viewModel.onSearchQueryChange("STREAM")
        assertEquals(2, viewModel.streams.size)
    }

    @Test
    fun `StreamListViewModel toggles year filter`() = runTest {
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = emptyList()))
        advanceUntilIdle()

        assertTrue(viewModel.selectedYears.isEmpty())

        viewModel.onYearToggle("2024")
        assertTrue(viewModel.selectedYears.contains("2024"))

        viewModel.onYearToggle("2024")
        assertFalse(viewModel.selectedYears.contains("2024"))
    }

    @Test
    fun `StreamListViewModel toggles market filter`() = runTest {
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = emptyList()))
        advanceUntilIdle()

        viewModel.onMarketToggle("AutoNet")
        assertTrue(viewModel.selectedMarkets.contains("AutoNet"))

        viewModel.onMarketToggle("AutoNet")
        assertFalse(viewModel.selectedMarkets.contains("AutoNet"))
    }

    @Test
    fun `StreamListViewModel toggles trl filter`() = runTest {
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = emptyList()))
        advanceUntilIdle()

        viewModel.onTrlToggle("3-5")
        assertTrue(viewModel.selectedTrls.contains("3-5"))

        viewModel.onTrlToggle("3-5")
        assertFalse(viewModel.selectedTrls.contains("3-5"))
    }

    @Test
    fun `StreamListViewModel resetFilters clears all filters`() = runTest {
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = emptyList()))
        advanceUntilIdle()

        viewModel.onSearchQueryChange("test")
        viewModel.onYearToggle("2024")
        viewModel.onMarketToggle("AutoNet")
        viewModel.onTrlToggle("3-5")

        viewModel.resetFilters()
        advanceUntilIdle()

        assertEquals("", viewModel.searchQuery)
        assertTrue(viewModel.selectedYears.isEmpty())
        assertTrue(viewModel.selectedMarkets.isEmpty())
        assertTrue(viewModel.selectedTrls.isEmpty())
    }

    @Test
    fun `StreamListViewModel load failure sets error message`() = runTest {
        val viewModel = StreamListViewModel(FakeStreamRepository(getResult = Result.failure(Exception("fail"))))
        advanceUntilIdle()

        assertTrue(viewModel.streams.isEmpty())
        assertEquals("Не удалось загрузить потоки", viewModel.errorMessage)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `StreamListViewModel loads markets on init`() = runTest {
        val markets = listOf(
            NtiMarket("m1", "autonet", "AutoNet"),
            NtiMarket("m2", "healthnet", "HealthNet")
        )
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = emptyList(), markets = markets))
        advanceUntilIdle()

        assertEquals(2, viewModel.availableMarkets.size)
        assertTrue(viewModel.availableMarkets.contains("AutoNet"))
        assertTrue(viewModel.availableMarkets.contains("HealthNet"))
    }

    @Test
    fun `StreamListViewModel hasMore is false when all pages loaded`() = runTest {
        val streams = listOf(sampleStream(id = "s1", name = "Only One"))
        val viewModel = StreamListViewModel(FakeStreamRepository(
            streams = streams,
            totalPages = 1
        ))
        advanceUntilIdle()

        assertFalse(viewModel.hasMore)
    }

    @Test
    fun `StreamListViewModel isLoading is true during initial load`() = runTest {
        val viewModel = StreamListViewModel(FakeStreamRepository(
            streams = listOf(sampleStream(id = "s1", name = "S"))
        ))
        assertTrue(viewModel.isLoading)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `StreamListViewModel onYearsChange sets selected years`() = runTest {
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = emptyList()))
        advanceUntilIdle()

        viewModel.onYearsChange(setOf("2024", "2025"))
        assertEquals(setOf("2024", "2025"), viewModel.selectedYears)
    }

    @Test
    fun `StreamListViewModel onMarketsChange sets selected markets`() = runTest {
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = emptyList()))
        advanceUntilIdle()

        viewModel.onMarketsChange(setOf("AutoNet", "HealthNet"))
        assertEquals(setOf("AutoNet", "HealthNet"), viewModel.selectedMarkets)
    }

    @Test
    fun `StreamListViewModel onTrlsChange sets selected trls`() = runTest {
        val viewModel = StreamListViewModel(FakeStreamRepository(streams = emptyList()))
        advanceUntilIdle()

        viewModel.onTrlsChange(setOf("3-5", "6-8"))
        assertEquals(setOf("3-5", "6-8"), viewModel.selectedTrls)
    }

    // ==================== Helper Methods ====================

    companion object {
        fun sampleStream(
            id: String = "s1",
            name: String = "Stream",
            active: Boolean = true
        ) = Stream(
            id = id,
            name = name,
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            description = "Description",
            active = active,
            trackStartDate = "2024-01-15",
            meetingsCount = 10,
            ntiMarkets = emptyList()
        )
    }
}

/**
 * Fake implementation of [StreamRepository] for testing [StreamListViewModel].
 * Defined as a top-level class (not inner) to avoid "Outer class cannot be used as receiver" errors.
 */
class FakeStreamRepository(
    val streams: List<Stream> = emptyList(),
    val markets: List<NtiMarket> = emptyList(),
    val getResult: Result<StreamPage>? = null,
    val totalPages: Int = 1
) : StreamRepository {
    override suspend fun getStreams(
        filters: List<StreamFilter>, page: Int, size: Int
    ): Result<StreamPage> = getResult ?: Result.success(
        StreamPage(streams, totalPages, streams.size.toLong(), 0)
    )

    override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(markets)
    override suspend fun createStream(request: StreamCreateRequest): Result<Stream> =
        Result.success(StreamComponentsUiTest.Companion.sampleStream())
    override suspend fun getStream(id: String): Result<Stream> = Result.success(StreamComponentsUiTest.Companion.sampleStream())
    override suspend fun updateStream(id: String, request: StreamCreateRequest): Result<Stream> =
        Result.success(StreamComponentsUiTest.Companion.sampleStream())
    override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = Result.success(Unit)
    override suspend fun getStreamImage(streamId: String): Result<ByteArray?> = Result.success(null)
    override suspend fun deleteStream(streamId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getTeamsByStream(streamId: String): Result<List<TeamCard>> = Result.success(emptyList())
}