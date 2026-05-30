package com.example.track_me_mobile.features.reports.presentation

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.features.reports.domain.ReportRepository
import com.example.track_me_mobile.features.reports.domain.StreamMeetingReportRepository
import com.example.track_me_mobile.features.reports.domain.models.ReportItem
import com.example.track_me_mobile.features.reports.domain.models.StreamMeetingReportItem
import io.ktor.client.*
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
class ReportsViewModelBigTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── ReportsViewModel: init / loadReports ─────────────────────────────────

    @Test
    fun `ReportsViewModel init loads reports successfully`() = runTest {
        val items = listOf(
            sampleReportItem(streamName = "Stream A"),
            sampleReportItem(streamName = "Stream B")
        )
        val repo = FakeReportRepository(getReportsResult = Result.success(items))
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        assertIs<ReportsState.Success>(vm.state)
        assertEquals(2, (vm.state as ReportsState.Success).reports.size)
    }

    @Test
    fun `ReportsViewModel init failure shows error`() = runTest {
        val repo = FakeReportRepository(getReportsResult = Result.failure(Exception("fail")))
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        assertIs<ReportsState.Error>(vm.state)
        assertEquals("Не удалось загрузить отчёты", (vm.state as ReportsState.Error).message)
    }

    @Test
    fun `ReportsViewModel loadReports updates state`() = runTest {
        val items = listOf(sampleReportItem(streamName = "S1"))
        val repo = FakeReportRepository(getReportsResult = Result.success(items))
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        val newItems = listOf(sampleReportItem(streamName = "S2"))
        repo.getReportsResult = Result.success(newItems)
        vm.loadReports()
        advanceUntilIdle()

        assertIs<ReportsState.Success>(vm.state)
        assertEquals("S2", (vm.state as ReportsState.Success).reports.first().streamName)
    }

    @Test
    fun `ReportsViewModel loadReports failure after success shows error`() = runTest {
        val items = listOf(sampleReportItem())
        val repo = FakeReportRepository(getReportsResult = Result.success(items))
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        repo.getReportsResult = Result.failure(Exception("fail"))
        vm.loadReports()
        advanceUntilIdle()

        assertIs<ReportsState.Error>(vm.state)
    }

    @Test
    fun `ReportsViewModel loadReports with empty list`() = runTest {
        val repo = FakeReportRepository(getReportsResult = Result.success(emptyList()))
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        assertIs<ReportsState.Success>(vm.state)
        assertTrue((vm.state as ReportsState.Success).reports.isEmpty())
    }

    // ─── ReportsViewModel: filters ────────────────────────────────────────────

    @Test
    fun `ReportsViewModel setTrackerFilter reloads reports`() = runTest {
        var lastTracker: String? = null
        val repo = object : ReportRepository {
            override suspend fun getReports(
                trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
            ): Result<List<ReportItem>> {
                lastTracker = trackerUsername
                return Result.success(listOf(sampleReportItem()))
            }
            override suspend fun downloadReportsExcel(
                trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
            ): Result<ByteArray> = Result.success(ByteArray(0))
        }
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        vm.setTrackerFilter("tracker1")
        advanceUntilIdle()

        assertEquals("tracker1", lastTracker)
        assertEquals("tracker1", vm.selectedTracker)
    }

    @Test
    fun `ReportsViewModel setTrackerFilter to All passes null`() = runTest {
        var lastTracker: String? = "not-null"
        val repo = object : ReportRepository {
            override suspend fun getReports(
                trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
            ): Result<List<ReportItem>> {
                lastTracker = trackerUsername
                return Result.success(listOf(sampleReportItem()))
            }
            override suspend fun downloadReportsExcel(
                trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
            ): Result<ByteArray> = Result.success(ByteArray(0))
        }
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        vm.setTrackerFilter("Все")
        advanceUntilIdle()

        assertNull(lastTracker)
    }

    @Test
    fun `ReportsViewModel setStreamFilter reloads reports`() = runTest {
        var lastStream: String? = null
        val repo = object : ReportRepository {
            override suspend fun getReports(
                trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
            ): Result<List<ReportItem>> {
                lastStream = streamName
                return Result.success(listOf(sampleReportItem()))
            }
            override suspend fun downloadReportsExcel(
                trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
            ): Result<ByteArray> = Result.success(ByteArray(0))
        }
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        vm.setStreamFilter("Stream A")
        advanceUntilIdle()

        assertEquals("Stream A", lastStream)
        assertEquals("Stream A", vm.selectedStream)
    }

    @Test
    fun `ReportsViewModel setStreamFilter to All passes null`() = runTest {
        var lastStream: String? = "not-null"
        val repo = object : ReportRepository {
            override suspend fun getReports(
                trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
            ): Result<List<ReportItem>> {
                lastStream = streamName
                return Result.success(listOf(sampleReportItem()))
            }
            override suspend fun downloadReportsExcel(
                trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
            ): Result<ByteArray> = Result.success(ByteArray(0))
        }
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        vm.setStreamFilter("Все")
        advanceUntilIdle()

        assertNull(lastStream)
    }

    @Test
    fun `ReportsViewModel toggleShowInactive flips flag and reloads`() = runTest {
        var lastShowInactive = false
        val repo = object : ReportRepository {
            override suspend fun getReports(
                trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
            ): Result<List<ReportItem>> {
                lastShowInactive = showInactive
                return Result.success(listOf(sampleReportItem()))
            }
            override suspend fun downloadReportsExcel(
                trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
            ): Result<ByteArray> = Result.success(ByteArray(0))
        }
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        assertFalse(vm.showInactive)
        assertFalse(lastShowInactive)

        vm.toggleShowInactive()
        advanceUntilIdle()

        assertTrue(vm.showInactive)
        assertTrue(lastShowInactive)
    }

    @Test
    fun `ReportsViewModel toggleShowInactive twice returns to false`() = runTest {
        val repo = FakeReportRepository(getReportsResult = Result.success(listOf(sampleReportItem())))
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        vm.toggleShowInactive()
        advanceUntilIdle()
        assertTrue(vm.showInactive)

        vm.toggleShowInactive()
        advanceUntilIdle()
        assertFalse(vm.showInactive)
    }

    @Test
    fun `ReportsViewModel availableStreams populated from reports`() = runTest {
        val items = listOf(
            sampleReportItem(streamName = "Stream B"),
            sampleReportItem(streamName = "Stream A"),
            sampleReportItem(streamName = "Stream B")
        )
        val repo = FakeReportRepository(getReportsResult = Result.success(items))
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        assertTrue("Все" in vm.availableStreams)
        assertTrue("Stream A" in vm.availableStreams)
        assertTrue("Stream B" in vm.availableStreams)
        assertEquals(3, vm.availableStreams.size) // "Все" + "Stream A" + "Stream B"
    }

    @Test
    fun `ReportsViewModel availableStreams with empty reports`() = runTest {
        val repo = FakeReportRepository(getReportsResult = Result.success(emptyList()))
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        assertEquals(listOf("Все"), vm.availableStreams)
    }

    // ─── ReportsViewModel: prepareReportExcel ─────────────────────────────────

    @Test
    fun `ReportsViewModel prepareReportExcel success`() = runTest {
        val repo = FakeReportRepository(
            getReportsResult = Result.success(listOf(sampleReportItem())),
            downloadExcelResult = Result.success(byteArrayOf(1, 2, 3))
        )
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        val result = vm.prepareReportExcel()
        assertTrue(result.isSuccess)
        val (fileName, bytes) = result.getOrThrow()
        assertTrue(fileName.contains("все_потоки"))
        assertTrue(bytes.contentEquals(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun `ReportsViewModel prepareReportExcel with stream filter`() = runTest {
        val repo = FakeReportRepository(
            getReportsResult = Result.success(listOf(sampleReportItem(streamName = "MyStream"))),
            downloadExcelResult = Result.success(byteArrayOf(1, 2, 3))
        )
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        vm.setStreamFilter("MyStream")
        advanceUntilIdle()

        val result = vm.prepareReportExcel()
        assertTrue(result.isSuccess)
        val (fileName, _) = result.getOrThrow()
        assertTrue(fileName.contains("MyStream"))
    }

    @Test
    fun `ReportsViewModel prepareReportExcel failure`() = runTest {
        val repo = FakeReportRepository(
            getReportsResult = Result.success(listOf(sampleReportItem())),
            downloadExcelResult = Result.failure(Exception("download failed"))
        )
        val vm = ReportsViewModel(repo, UserInfoHolder(), HttpClient())
        advanceUntilIdle()

        val result = vm.prepareReportExcel()
        assertTrue(result.isFailure)
    }

    // ─── StreamMeetingReportViewModel: init / loadReports ─────────────────────

    @Test
    fun `StreamMeetingReportViewModel init loads reports successfully`() = runTest {
        val items = listOf(
            sampleStreamReportItem(teamName = "Team A", trackerName = "tracker1"),
            sampleStreamReportItem(teamName = "Team B", trackerName = "tracker2")
        )
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        assertIs<StreamMeetingReportState.Success>(vm.state)
        assertEquals(2, (vm.state as StreamMeetingReportState.Success).items.size)
    }

    @Test
    fun `StreamMeetingReportViewModel init failure shows error`() = runTest {
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.failure(Exception("fail")))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        assertIs<StreamMeetingReportState.Error>(vm.state)
        assertEquals("Не удалось загрузить отчёт по встречам", (vm.state as StreamMeetingReportState.Error).message)
    }

    @Test
    fun `StreamMeetingReportViewModel loadReports reloads`() = runTest {
        val items = listOf(sampleStreamReportItem(teamName = "T1"))
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        val newItems = listOf(sampleStreamReportItem(teamName = "T2"))
        repo.getReportsResult = Result.success(newItems)
        vm.loadReports()
        advanceUntilIdle()

        assertIs<StreamMeetingReportState.Success>(vm.state)
        assertEquals("T2", (vm.state as StreamMeetingReportState.Success).items.first().teamName)
    }

    @Test
    fun `StreamMeetingReportViewModel loadReports with empty list`() = runTest {
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(emptyList()))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        assertIs<StreamMeetingReportState.Success>(vm.state)
        assertTrue((vm.state as StreamMeetingReportState.Success).items.isEmpty())
    }

    // ─── StreamMeetingReportViewModel: filters ────────────────────────────────

    @Test
    fun `StreamMeetingReportViewModel setTrackerFilter filters items`() = runTest {
        val items = listOf(
            sampleStreamReportItem(teamName = "Team A", trackerName = "tracker1", trackerFullName = "Tracker One"),
            sampleStreamReportItem(teamName = "Team B", trackerName = "tracker2", trackerFullName = "Tracker Two")
        )
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        vm.setTrackerFilter("Tracker One")
        assertEquals(1, (vm.state as StreamMeetingReportState.Success).items.size)
        assertEquals("Team A", (vm.state as StreamMeetingReportState.Success).items.first().teamName)
    }

    @Test
    fun `StreamMeetingReportViewModel setTeamFilter filters items`() = runTest {
        val items = listOf(
            sampleStreamReportItem(teamName = "Team A"),
            sampleStreamReportItem(teamName = "Team B")
        )
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        vm.setTeamFilter("Team B")
        assertEquals(1, (vm.state as StreamMeetingReportState.Success).items.size)
        assertEquals("Team B", (vm.state as StreamMeetingReportState.Success).items.first().teamName)
    }

    @Test
    fun `StreamMeetingReportViewModel setStatusFilter filters items`() = runTest {
        val items = listOf(
            sampleStreamReportItem(teamName = "Team A", teamStatus = "Всё ок"),
            sampleStreamReportItem(teamName = "Team B", teamStatus = "Есть проблемы")
        )
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        vm.setStatusFilter("Есть проблемы")
        assertEquals(1, (vm.state as StreamMeetingReportState.Success).items.size)
        assertEquals("Team B", (vm.state as StreamMeetingReportState.Success).items.first().teamName)
    }

    @Test
    fun `StreamMeetingReportViewModel setTrackerFilter to All shows all`() = runTest {
        val items = listOf(
            sampleStreamReportItem(teamName = "Team A", trackerName = "t1"),
            sampleStreamReportItem(teamName = "Team B", trackerName = "t2")
        )
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        vm.setTrackerFilter("Все")
        assertEquals(2, (vm.state as StreamMeetingReportState.Success).items.size)
    }

    @Test
    fun `StreamMeetingReportViewModel setTeamFilter to All shows all`() = runTest {
        val items = listOf(
            sampleStreamReportItem(teamName = "Team A"),
            sampleStreamReportItem(teamName = "Team B")
        )
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        vm.setTeamFilter("Все")
        assertEquals(2, (vm.state as StreamMeetingReportState.Success).items.size)
    }

    @Test
    fun `StreamMeetingReportViewModel setStatusFilter to All shows all`() = runTest {
        val items = listOf(
            sampleStreamReportItem(teamName = "Team A", teamStatus = "Всё ок"),
            sampleStreamReportItem(teamName = "Team B", teamStatus = "Есть проблемы")
        )
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        vm.setStatusFilter("Все")
        assertEquals(2, (vm.state as StreamMeetingReportState.Success).items.size)
    }

    @Test
    fun `StreamMeetingReportViewModel available filters populated`() = runTest {
        val items = listOf(
            sampleStreamReportItem(teamName = "Team B", trackerName = "t1", trackerFullName = "Tracker B", teamStatus = "Всё ок"),
            sampleStreamReportItem(teamName = "Team A", trackerName = "t2", trackerFullName = "Tracker A", teamStatus = "Есть проблемы")
        )
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        assertTrue("Все" in vm.availableTrackers)
        assertTrue("Tracker A" in vm.availableTrackers)
        assertTrue("Tracker B" in vm.availableTrackers)

        assertTrue("Все" in vm.availableTeams)
        assertTrue("Team A" in vm.availableTeams)
        assertTrue("Team B" in vm.availableTeams)

        assertTrue("Все" in vm.availableStatuses)
        assertTrue("Всё ок" in vm.availableStatuses)
        assertTrue("Есть проблемы" in vm.availableStatuses)
    }

    @Test
    fun `StreamMeetingReportViewModel uses trackerName when trackerFullName is null`() = runTest {
        val items = listOf(
            sampleStreamReportItem(teamName = "Team A", trackerName = "tracker1", trackerFullName = null)
        )
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        assertTrue("tracker1" in vm.availableTrackers)
    }

    @Test
    fun `StreamMeetingReportViewModel uses status when teamStatus is null`() = runTest {
        val items = listOf(
            sampleStreamReportItem(teamName = "Team A", teamStatus = null, status = "some-status")
        )
        val repo = FakeStreamMeetingReportRepository(getReportsResult = Result.success(items))
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        assertTrue("some-status" in vm.availableStatuses)
    }

    // ─── StreamMeetingReportViewModel: prepareExcelReport ─────────────────────

    @Test
    fun `StreamMeetingReportViewModel prepareExcelReport success`() = runTest {
        val repo = FakeStreamMeetingReportRepository(
            getReportsResult = Result.success(listOf(sampleStreamReportItem())),
            downloadExcelResult = Result.success(byteArrayOf(1, 2, 3))
        )
        val vm = StreamMeetingReportViewModel(repo, "stream-12345678")
        advanceUntilIdle()

        val result = vm.prepareExcelReport()
        assertTrue(result.isSuccess)
        val (fileName, bytes) = result.getOrThrow()
        assertTrue(fileName.contains("stream-1")) // take(8) of "stream-12345678" = "stream-1"
        assertTrue(bytes.contentEquals(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun `StreamMeetingReportViewModel prepareExcelReport failure`() = runTest {
        val repo = FakeStreamMeetingReportRepository(
            getReportsResult = Result.success(listOf(sampleStreamReportItem())),
            downloadExcelResult = Result.failure(Exception("fail"))
        )
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        val result = vm.prepareExcelReport()
        assertTrue(result.isFailure)
    }

    @Test
    fun `StreamMeetingReportViewModel prepareExcelReport with filters`() = runTest {
        var capturedTrackerFilter: String? = null
        var capturedTeamFilter: String? = null
        var capturedStatusFilter: String? = null
        val repo = object : StreamMeetingReportRepository {
            override suspend fun getReportsByStream(
                streamId: String, page: Int, size: Int, sort: List<String>
            ): Result<List<StreamMeetingReportItem>> {
                return Result.success(listOf(
                    sampleStreamReportItem(teamName = "Team A", trackerName = "t1", trackerFullName = "Tracker One", teamStatus = "Всё ок")
                ))
            }
            override suspend fun downloadReportsExcel(
                streamId: String, trackerFilter: String?, teamFilter: String?,
                statusFilter: String?, page: Int, size: Int, sort: List<String>
            ): Result<ByteArray> {
                capturedTrackerFilter = trackerFilter
                capturedTeamFilter = teamFilter
                capturedStatusFilter = statusFilter
                return Result.success(ByteArray(0))
            }
        }
        val vm = StreamMeetingReportViewModel(repo, "stream-1")
        advanceUntilIdle()

        vm.setTrackerFilter("Tracker One")
        vm.setTeamFilter("Team A")
        vm.setStatusFilter("Всё ок")

        vm.prepareExcelReport()
        assertEquals("Tracker One", capturedTrackerFilter)
        assertEquals("Team A", capturedTeamFilter)
        assertEquals("Всё ок", capturedStatusFilter)
    }

    // ─── Helper classes ───────────────────────────────────────────────────────

    private fun sampleReportItem(
        streamName: String = "Stream"
    ) = ReportItem(
        streamName = streamName,
        startDate = "2024-01-01",
        endDate = "2024-01-02",
        teamCardName = "Team",
        username = "tracker",
        averageTeamGrade = 4.0,
        averageUserGrade = 3.5,
        meetingsCountPlan = 5,
        meetingsCountFact = 3,
        ntiMarkets = listOf("Market"),
        readinessLevel = "5"
    )

    private fun sampleStreamReportItem(
        teamName: String = "Team",
        trackerName: String? = "tracker",
        trackerFullName: String? = "Tracker Full",
        teamStatus: String? = "Всё ок",
        status: String? = "Проведена"
    ) = StreamMeetingReportItem(
        teamName = teamName,
        trackerName = trackerName,
        trackerFullName = trackerFullName,
        startDate = "2024-01-01",
        teamStatus = teamStatus,
        status = status
    )

    private class FakeReportRepository(
        var getReportsResult: Result<List<ReportItem>> = Result.failure(Exception("not configured")),
        private val downloadExcelResult: Result<ByteArray> = Result.failure(Exception("not configured"))
    ) : ReportRepository {
        override suspend fun getReports(
            trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
        ): Result<List<ReportItem>> = getReportsResult

        override suspend fun downloadReportsExcel(
            trackerUsername: String?, streamName: String?, page: Int, size: Int, showInactive: Boolean
        ): Result<ByteArray> = downloadExcelResult
    }

    private class FakeStreamMeetingReportRepository(
        var getReportsResult: Result<List<StreamMeetingReportItem>> = Result.failure(Exception("not configured")),
        private val downloadExcelResult: Result<ByteArray> = Result.failure(Exception("not configured"))
    ) : StreamMeetingReportRepository {
        override suspend fun getReportsByStream(
            streamId: String, page: Int, size: Int, sort: List<String>
        ): Result<List<StreamMeetingReportItem>> = getReportsResult

        override suspend fun downloadReportsExcel(
            streamId: String, trackerFilter: String?, teamFilter: String?,
            statusFilter: String?, page: Int, size: Int, sort: List<String>
        ): Result<ByteArray> = downloadExcelResult
    }
}