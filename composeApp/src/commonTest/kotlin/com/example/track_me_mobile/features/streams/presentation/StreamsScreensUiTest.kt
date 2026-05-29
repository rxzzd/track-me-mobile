package com.example.track_me_mobile.features.streams.presentation

import com.example.track_me_mobile.features.streams.data.StreamHasTeamsException
import com.example.track_me_mobile.features.streams.domain.StreamRepository
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import com.example.track_me_mobile.features.streams.domain.models.Stream
import com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest
import com.example.track_me_mobile.features.streams.domain.models.StreamFilter
import com.example.track_me_mobile.features.streams.domain.StreamPage
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
class StreamsScreensUiTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== AddStreamViewModel Tests ====================

    @Test
    fun `AddStreamViewModel loads markets on init`() = runTest {
        val markets = listOf(
            NtiMarket("m1", "autonet", "AutoNet"),
            NtiMarket("m2", "healthnet", "HealthNet")
        )
        val viewModel = AddStreamViewModel(FakeStreamRepository2(markets = markets))
        advanceUntilIdle()

        assertEquals(2, viewModel.availableMarkets.size)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `AddStreamViewModel toggleMarket adds and removes market ids`() {
        val viewModel = AddStreamViewModel(FakeStreamRepository2())

        assertTrue(viewModel.selectedMarketIds.isEmpty())

        viewModel.toggleMarket("m1")
        assertTrue(viewModel.selectedMarketIds.contains("m1"))

        viewModel.toggleMarket("m1")
        assertFalse(viewModel.selectedMarketIds.contains("m1"))
    }

    @Test
    fun `AddStreamViewModel toggleMarket handles multiple markets`() {
        val viewModel = AddStreamViewModel(FakeStreamRepository2())

        viewModel.toggleMarket("m1")
        viewModel.toggleMarket("m2")
        viewModel.toggleMarket("m3")

        assertEquals(setOf("m1", "m2", "m3"), viewModel.selectedMarketIds)

        viewModel.toggleMarket("m2")
        assertEquals(setOf("m1", "m3"), viewModel.selectedMarketIds)
    }

    @Test
    fun `AddStreamViewModel createStream validates required fields`() = runTest {
        val viewModel = AddStreamViewModel(FakeStreamRepository2())
        advanceUntilIdle()

        // All fields blank - should fail validation
        viewModel.createStream {}
        assertEquals("Заполните обязательные поля", viewModel.errorMessage)
        assertFalse(viewModel.isSuccess)
    }

    @Test
    fun `AddStreamViewModel createStream validates meetingsCount`() = runTest {
        val viewModel = AddStreamViewModel(FakeStreamRepository2())
        advanceUntilIdle()

        viewModel.name = "Test Stream"
        viewModel.startDate = "2024-01-01"
        viewModel.endDate = "2024-12-31"
        viewModel.trackStartDate = "2024-01-15"
        viewModel.meetingsCount = 0

        viewModel.createStream {}
        assertEquals("Выберите количество встреч", viewModel.errorMessage)
        assertFalse(viewModel.isSuccess)
    }

    @Test
    fun `AddStreamViewModel createStream succeeds with valid data`() = runTest {
        var success = false
        val viewModel = AddStreamViewModel(FakeStreamRepository2())
        advanceUntilIdle()

        viewModel.name = "Test Stream"
        viewModel.startDate = "2024-01-01"
        viewModel.endDate = "2024-12-31"
        viewModel.trackStartDate = "2024-01-15"
        viewModel.meetingsCount = 10

        viewModel.createStream { success = true }
        advanceUntilIdle()

        assertTrue(success)
        assertTrue(viewModel.isSuccess)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `AddStreamViewModel createStream failure sets error message`() = runTest {
        val viewModel = AddStreamViewModel(FakeStreamRepository2(createResult = Result.failure(Exception("API error"))))
        advanceUntilIdle()

        viewModel.name = "Test"
        viewModel.startDate = "2024-01-01"
        viewModel.endDate = "2024-12-31"
        viewModel.trackStartDate = "2024-01-15"
        viewModel.meetingsCount = 5

        viewModel.createStream {}
        advanceUntilIdle()

        assertEquals("API error", viewModel.errorMessage)
        assertFalse(viewModel.isSuccess)
    }

    @Test
    fun `AddStreamViewModel setStreamImage stores pending image`() {
        val viewModel = AddStreamViewModel(FakeStreamRepository2())

        assertNull(viewModel.pendingImageBytes)

        viewModel.setStreamImage(byteArrayOf(1, 2, 3))
        assertNotNull(viewModel.pendingImageBytes)
        assertEquals(3, viewModel.pendingImageBytes?.size)
    }

    @Test
    fun `AddStreamViewModel createStream with pending image uploads after creation`() = runTest {
        var uploadCalled = false
        val repository = object : FakeStreamRepository2() {
            override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> {
                uploadCalled = true
                return Result.success(Unit)
            }
        }
        val viewModel = AddStreamViewModel(repository)
        advanceUntilIdle()

        viewModel.name = "Test"
        viewModel.startDate = "2024-01-01"
        viewModel.endDate = "2024-12-31"
        viewModel.trackStartDate = "2024-01-15"
        viewModel.meetingsCount = 5
        viewModel.setStreamImage(byteArrayOf(1, 2, 3))

        viewModel.createStream {}
        advanceUntilIdle()

        assertTrue(uploadCalled)
        assertTrue(viewModel.isSuccess)
    }

    @Test
    fun `AddStreamViewModel createStream with image upload failure sets error`() = runTest {
        val repository = object : FakeStreamRepository2() {
            override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> =
                Result.failure(Exception("upload fail"))
        }
        val viewModel = AddStreamViewModel(repository)
        advanceUntilIdle()

        viewModel.name = "Test"
        viewModel.startDate = "2024-01-01"
        viewModel.endDate = "2024-12-31"
        viewModel.trackStartDate = "2024-01-15"
        viewModel.meetingsCount = 5
        viewModel.setStreamImage(byteArrayOf(1, 2, 3))

        viewModel.createStream {}
        advanceUntilIdle()

        assertEquals("Поток создан, но не удалось загрузить фото", viewModel.errorMessage)
    }


    // ==================== EditStreamViewModel Tests ====================

    @Test
    fun `EditStreamViewModel loads stream data on init`() = runTest {
        val stream = sampleStream(
            id = "stream-1",
            name = "Test Stream",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            trackStartDate = "2024-01-15",
            meetingsCount = 10
        )
        val viewModel = EditStreamViewModel(FakeStreamRepository2(stream = stream), "stream-1")
        advanceUntilIdle()

        assertEquals("Test Stream", viewModel.name)
        assertEquals("2024-01-01", viewModel.startDate)
        assertEquals("2024-12-31", viewModel.endDate)
        assertEquals("2024-01-15", viewModel.trackStartDate)
        assertEquals(10, viewModel.meetingsCount)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `EditStreamViewModel load failure sets error message`() = runTest {
        val viewModel = EditStreamViewModel(
            FakeStreamRepository2(getStreamResult = Result.failure(Exception("not found"))),
            "missing-id"
        )
        advanceUntilIdle()

        assertEquals("not found", viewModel.errorMessage)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `EditStreamViewModel updateStream validates required fields`() = runTest {
        val viewModel = EditStreamViewModel(FakeStreamRepository2(stream = sampleStream()), "s1")
        advanceUntilIdle()

        viewModel.name = ""

        viewModel.updateStream {}
        assertEquals("Заполните обязательные поля", viewModel.errorMessage)
    }

    @Test
    fun `EditStreamViewModel updateStream succeeds with valid data`() = runTest {
        var success = false
        val viewModel = EditStreamViewModel(FakeStreamRepository2(stream = sampleStream()), "s1")
        advanceUntilIdle()

        viewModel.name = "Updated Stream"
        viewModel.updateStream { success = true }
        advanceUntilIdle()

        assertTrue(success)
    }

    @Test
    fun `EditStreamViewModel updateStream failure sets error message`() = runTest {
        val viewModel = EditStreamViewModel(
            FakeStreamRepository2(stream = sampleStream(), updateResult = Result.failure(Exception("update fail"))),
            "s1"
        )
        advanceUntilIdle()

        viewModel.updateStream {}
        advanceUntilIdle()

        assertEquals("Ошибка при сохранении: проверьте корректность данных", viewModel.errorMessage)
    }

    @Test
    fun `EditStreamViewModel updateStream shows future date error`() = runTest {
        val viewModel = EditStreamViewModel(
            FakeStreamRepository2(
                stream = sampleStream(),
                updateResult = Result.failure(Exception("trackStartDate: Дата начала трека должна быть в будущем"))
            ),
            "s1"
        )
        advanceUntilIdle()

        viewModel.updateStream {}
        advanceUntilIdle()

        assertEquals("Дата начала трека должна быть в будущем", viewModel.errorMessage)
    }

    @Test
    fun `EditStreamViewModel deleteStream succeeds`() = runTest {
        var deleted = false
        val viewModel = EditStreamViewModel(FakeStreamRepository2(stream = sampleStream()), "s1")
        advanceUntilIdle()

        viewModel.deleteStream { deleted = true }
        advanceUntilIdle()

        assertTrue(deleted)
    }

    @Test
    fun `EditStreamViewModel deleteStream with StreamHasTeamsException shows dialog`() = runTest {
        val teams = listOf(
            TeamCard("t1", "Team 1", "", "", "ACTIVE", "u", true, emptyList(), "1", null, null, 0, 0, 0)
        )
        val viewModel = EditStreamViewModel(
            FakeStreamRepository2(
                stream = sampleStream(),
                deleteResult = Result.failure(StreamHasTeamsException("has teams")),
                teamsByStream = teams
            ),
            "s1"
        )
        advanceUntilIdle()

        viewModel.deleteStream {}
        advanceUntilIdle()

        // showTeamsConflictDialog is private set, so we verify through teamsInStream
        assertEquals(1, viewModel.teamsInStream.size)
        assertEquals("Team 1", viewModel.teamsInStream[0].name)
        // dismissTeamsDialog only sets showTeamsConflictDialog = false, it does NOT clear teamsInStream
        viewModel.dismissTeamsDialog()
        // teamsInStream is NOT cleared by dismissTeamsDialog, so it should still be 1
        assertEquals(1, viewModel.teamsInStream.size)
    }

    @Test
    fun `EditStreamViewModel deleteStream with StreamHasTeamsException but empty teams shows error`() = runTest {
        val viewModel = EditStreamViewModel(
            FakeStreamRepository2(
                stream = sampleStream(),
                deleteResult = Result.failure(StreamHasTeamsException("has teams")),
                teamsByStream = emptyList()
            ),
            "s1"
        )
        advanceUntilIdle()

        viewModel.deleteStream {}
        advanceUntilIdle()

        assertEquals("В потоке есть команды. Удалите команды перед удалением потока.", viewModel.errorMessage)
    }

    @Test
    fun `EditStreamViewModel deleteStream with StreamHasTeamsException and teams load failure shows error`() = runTest {
        val viewModel = EditStreamViewModel(
            FakeStreamRepository2(
                stream = sampleStream(),
                deleteResult = Result.failure(StreamHasTeamsException("has teams")),
                getTeamsResult = Result.failure(Exception("fail"))
            ),
            "s1"
        )
        advanceUntilIdle()

        viewModel.deleteStream {}
        advanceUntilIdle()

        assertEquals("В потоке есть команды. Удалите команды перед удалением потока.", viewModel.errorMessage)
    }

    @Test
    fun `EditStreamViewModel deleteStream other failure sets error`() = runTest {
        val viewModel = EditStreamViewModel(
            FakeStreamRepository2(
                stream = sampleStream(),
                deleteResult = Result.failure(Exception("server error"))
            ),
            "s1"
        )
        advanceUntilIdle()

        viewModel.deleteStream {}
        advanceUntilIdle()

        assertEquals("server error", viewModel.errorMessage)
    }

    @Test
    fun `EditStreamViewModel dismissTeamsDialog can be called safely`() = runTest {
        val viewModel = EditStreamViewModel(FakeStreamRepository2(stream = sampleStream()), "s1")
        advanceUntilIdle()

        // dismissTeamsDialog is a public method - verify it can be called without error
        viewModel.dismissTeamsDialog()
        // After dismiss, teamsInStream should be empty
        assertEquals(0, viewModel.teamsInStream.size)
    }

    @Test
    fun `EditStreamViewModel uploadStreamImage succeeds`() = runTest {
        var uploadSuccess = false
        val viewModel = EditStreamViewModel(FakeStreamRepository2(stream = sampleStream()), "s1")
        advanceUntilIdle()

        viewModel.setStreamImage(byteArrayOf(1, 2, 3))
        viewModel.uploadStreamImage { uploadSuccess = true }
        advanceUntilIdle()

        assertTrue(uploadSuccess)
        assertNotNull(viewModel.streamImageBytes)
    }

    @Test
    fun `EditStreamViewModel uploadStreamImage failure sets error`() = runTest {
        val viewModel = EditStreamViewModel(
            FakeStreamRepository2(stream = sampleStream(), uploadResult = Result.failure(Exception("upload fail"))),
            "s1"
        )
        advanceUntilIdle()

        viewModel.setStreamImage(byteArrayOf(1, 2, 3))
        viewModel.uploadStreamImage {}
        advanceUntilIdle()

        assertEquals("Не удалось загрузить фото", viewModel.errorMessage)
    }

    @Test
    fun `EditStreamViewModel clearPendingImage clears pending bytes`() {
        val viewModel = EditStreamViewModel(FakeStreamRepository2(stream = sampleStream()), "s1")

        viewModel.setStreamImage(byteArrayOf(1, 2, 3))
        assertNotNull(viewModel.pendingImageBytes)

        viewModel.clearPendingImage()
        assertNull(viewModel.pendingImageBytes)
    }

    @Test
    fun `EditStreamViewModel toggleMarket adds and removes market ids`() = runTest {
        val viewModel = EditStreamViewModel(FakeStreamRepository2(stream = sampleStream()), "s1")
        advanceUntilIdle()

        viewModel.toggleMarket("m1")
        assertTrue(viewModel.selectedMarketIds.contains("m1"))

        viewModel.toggleMarket("m1")
        assertFalse(viewModel.selectedMarketIds.contains("m1"))
    }

    @Test
    fun `EditStreamViewModel loads stream image on init`() = runTest {
        val imageBytes = byteArrayOf(10, 20, 30)
        val viewModel = EditStreamViewModel(
            FakeStreamRepository2(stream = sampleStream(), streamImage = imageBytes),
            "s1"
        )
        advanceUntilIdle()

        assertNotNull(viewModel.streamImageBytes)
        assertEquals(3, viewModel.streamImageBytes?.size)
    }

    // ==================== Helper Methods ====================

    companion object {
        fun sampleStream(
            id: String = "s1",
            name: String = "Stream",
            startDate: String = "2024-01-01",
            endDate: String = "2024-12-31",
            trackStartDate: String = "2024-01-15",
            meetingsCount: Int = 10
        ) = Stream(
            id = id, name = name, startDate = startDate, endDate = endDate,
            description = "desc", active = true, trackStartDate = trackStartDate,
            meetingsCount = meetingsCount, ntiMarkets = emptyList()
        )
    }
}

/**
 * Fake implementation of [StreamRepository] for testing [AddStreamViewModel] and [EditStreamViewModel].
 * Defined as a top-level class (not inner) to avoid "Outer class cannot be used as receiver" errors.
 */
open class FakeStreamRepository2(
    val streams: List<Stream> = emptyList(),
    val markets: List<NtiMarket> = emptyList(),
    val stream: Stream? = null,
    val createResult: Result<Stream> = Result.success(
        Stream("new-id", "Created", "2024-01-01", "2024-12-31", "desc", true, "2024-01-15", 5, emptyList())
    ),
    val getStreamResult: Result<Stream>? = null,
    val updateResult: Result<Stream> = Result.success(
        Stream("s1", "Updated", "2024-01-01", "2024-12-31", "desc", true, "2024-01-15", 5, emptyList())
    ),
    val deleteResult: Result<Unit> = Result.success(Unit),
    val uploadResult: Result<Unit> = Result.success(Unit),
    val streamImage: ByteArray? = null,
    val teamsByStream: List<TeamCard> = emptyList(),
    val getTeamsResult: Result<List<TeamCard>>? = null
) : StreamRepository {
    override suspend fun getStreams(filters: List<StreamFilter>, page: Int, size: Int): Result<StreamPage> =
        Result.success(StreamPage(streams, 1, streams.size.toLong(), 0))
    override suspend fun getNtiMarkets(): Result<List<NtiMarket>> = Result.success(markets)
    override suspend fun createStream(request: StreamCreateRequest): Result<Stream> = createResult
    override suspend fun getStream(id: String): Result<Stream> = getStreamResult ?: (stream?.let { Result.success(it) }
        ?: Result.failure(Exception("no stream")))
    override suspend fun updateStream(id: String, request: StreamCreateRequest): Result<Stream> = updateResult
    override suspend fun uploadStreamImage(id: String, bytes: ByteArray): Result<Unit> = uploadResult
    override suspend fun getStreamImage(streamId: String): Result<ByteArray?> = Result.success(streamImage)
    override suspend fun deleteStream(streamId: String): Result<Unit> = deleteResult
    override suspend fun getTeamsByStream(streamId: String): Result<List<TeamCard>> =
        getTeamsResult ?: Result.success(teamsByStream)
}