package com.example.track_me_mobile.features.reports.presentation

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.features.reports.domain.ReportRepository
import com.example.track_me_mobile.features.reports.domain.StreamMeetingReportRepository
import com.example.track_me_mobile.features.reports.domain.models.ReportItem
import com.example.track_me_mobile.features.reports.domain.models.StreamMeetingReportItem
import com.example.track_me_mobile.testsupport.testHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
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
class ReportsComponentsUiTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== StreamMeetingReportViewModel Additional Edge Cases ====================

    @Test
    fun `StreamMeetingReportViewModel loads reports on init`() = runTest {
        val items = listOf(
            sampleMeetingReport(teamName = "Alpha", trackerFullName = "Alice", teamStatus = "OK"),
            sampleMeetingReport(teamName = "Beta", trackerFullName = "Bob", teamStatus = "ISSUES")
        )
        val viewModel = StreamMeetingReportViewModel(FakeStreamMeetingReportRepository(items), "stream-1")
        advanceUntilIdle()

        assertTrue(viewModel.state is StreamMeetingReportState.Success)
        assertEquals(2, (viewModel.state as StreamMeetingReportState.Success).items.size)
    }

    @Test
    fun `StreamMeetingReportViewModel filters by team`() = runTest {
        val items = listOf(
            sampleMeetingReport(teamName = "Alpha", trackerFullName = "Alice", teamStatus = "OK"),
            sampleMeetingReport(teamName = "Beta", trackerFullName = "Bob", teamStatus = "OK")
        )
        val viewModel = StreamMeetingReportViewModel(FakeStreamMeetingReportRepository(items), "s1")
        advanceUntilIdle()

        viewModel.setTeamFilter("Alpha")
        val state = viewModel.state as StreamMeetingReportState.Success
        assertEquals(1, state.items.size)
        assertEquals("Alpha", state.items.first().teamName)
    }

    @Test
    fun `StreamMeetingReportViewModel filters by status`() = runTest {
        val items = listOf(
            sampleMeetingReport(teamName = "Alpha", trackerFullName = "Alice", teamStatus = "OK"),
            sampleMeetingReport(teamName = "Beta", trackerFullName = "Bob", teamStatus = "ISSUES")
        )
        val viewModel = StreamMeetingReportViewModel(FakeStreamMeetingReportRepository(items), "s1")
        advanceUntilIdle()

        viewModel.setStatusFilter("ISSUES")
        val state = viewModel.state as StreamMeetingReportState.Success
        assertEquals(1, state.items.size)
        assertEquals("Beta", state.items.first().teamName)
    }

    @Test
    fun `StreamMeetingReportViewModel filters by all three criteria`() = runTest {
        val items = listOf(
            sampleMeetingReport(teamName = "Alpha", trackerFullName = "Alice", teamStatus = "OK"),
            sampleMeetingReport(teamName = "Alpha", trackerFullName = "Alice", teamStatus = "ISSUES"),
            sampleMeetingReport(teamName = "Beta", trackerFullName = "Bob", teamStatus = "OK")
        )
        val viewModel = StreamMeetingReportViewModel(FakeStreamMeetingReportRepository(items), "s1")
        advanceUntilIdle()

        viewModel.setTrackerFilter("Alice")
        viewModel.setTeamFilter("Alpha")
        viewModel.setStatusFilter("OK")
        val state = viewModel.state as StreamMeetingReportState.Success
        assertEquals(1, state.items.size)
    }

    @Test
    fun `StreamMeetingReportViewModel available filters populated`() = runTest {
        val items = listOf(
            sampleMeetingReport(teamName = "Alpha", trackerFullName = "Alice", teamStatus = "OK"),
            sampleMeetingReport(teamName = "Beta", trackerFullName = "Bob", teamStatus = "ISSUES")
        )
        val viewModel = StreamMeetingReportViewModel(FakeStreamMeetingReportRepository(items), "s1")
        advanceUntilIdle()

        assertTrue(viewModel.availableTrackers.contains("Alice"))
        assertTrue(viewModel.availableTrackers.contains("Bob"))
        assertTrue(viewModel.availableTeams.contains("Alpha"))
        assertTrue(viewModel.availableTeams.contains("Beta"))
        assertTrue(viewModel.availableStatuses.contains("OK"))
        assertTrue(viewModel.availableStatuses.contains("ISSUES"))
    }

    @Test
    fun `StreamMeetingReportViewModel loadReports reloads data`() = runTest {
        var callCount = 0
        val repository = object : StreamMeetingReportRepository {
            override suspend fun getReportsByStream(
                streamId: String, page: Int, size: Int, sort: List<String>
            ): Result<List<StreamMeetingReportItem>> {
                callCount++
                return Result.success(listOf(sampleMeetingReport()))
            }
            override suspend fun downloadReportsExcel(
                streamId: String, trackerFilter: String?, teamFilter: String?,
                statusFilter: String?, page: Int, size: Int, sort: List<String>
            ): Result<ByteArray> = Result.success(byteArrayOf())
        }
        val viewModel = StreamMeetingReportViewModel(repository, "s1")
        advanceUntilIdle()

        assertEquals(1, callCount)

        viewModel.loadReports()
        advanceUntilIdle()

        assertEquals(2, callCount)
    }

    @Test
    fun `StreamMeetingReportViewModel prepareExcelReport returns correct filename`() = runTest {
        val viewModel = StreamMeetingReportViewModel(
            FakeStreamMeetingReportRepository(
                items = listOf(sampleMeetingReport()),
                excelBytes = byteArrayOf(1, 2, 3)
            ),
            "stream-12345678"
        )
        advanceUntilIdle()

        val result = viewModel.prepareExcelReport()
        assertTrue(result.isSuccess)
        val (fileName, bytes) = result.getOrThrow()
        assertTrue(fileName.startsWith("Отчёт_встречи_"))
        assertEquals(3, bytes.size)
    }

    @Test
    fun `StreamMeetingReportViewModel prepareExcelReport failure`() = runTest {
        val repository = object : StreamMeetingReportRepository {
            override suspend fun getReportsByStream(
                streamId: String, page: Int, size: Int, sort: List<String>
            ): Result<List<StreamMeetingReportItem>> = Result.success(listOf(sampleMeetingReport()))
            override suspend fun downloadReportsExcel(
                streamId: String, trackerFilter: String?, teamFilter: String?,
                statusFilter: String?, page: Int, size: Int, sort: List<String>
            ): Result<ByteArray> = Result.failure(Exception("Download failed"))
        }
        val viewModel = StreamMeetingReportViewModel(repository, "s1")
        advanceUntilIdle()

        val result = viewModel.prepareExcelReport()
        assertTrue(result.isFailure)
    }

    @Test
    fun `StreamMeetingReportViewModel uses status fallback when teamStatus is null`() = runTest {
        val items = listOf(
            StreamMeetingReportItem(
                teamName = "Team",
                trackerName = "tracker",
                trackerFullName = "Tracker",
                teamStatus = null,
                status = "SCHEDULED"
            )
        )
        val viewModel = StreamMeetingReportViewModel(FakeStreamMeetingReportRepository(items), "s1")
        advanceUntilIdle()

        assertTrue(viewModel.availableStatuses.contains("SCHEDULED"))

        viewModel.setStatusFilter("SCHEDULED")
        assertEquals(1, (viewModel.state as StreamMeetingReportState.Success).items.size)
    }

    @Test
    fun `StreamMeetingReportViewModel resets invalid filter on reload`() = runTest {
        val items = listOf(
            sampleMeetingReport(teamName = "OnlyTeam", trackerFullName = "OnlyTracker", teamStatus = "OK")
        )
        val viewModel = StreamMeetingReportViewModel(FakeStreamMeetingReportRepository(items), "s1")
        advanceUntilIdle()

        viewModel.setTeamFilter("MissingTeam")
        viewModel.setStatusFilter("MissingStatus")

        viewModel.loadReports()
        advanceUntilIdle()

        assertEquals("Все", viewModel.selectedTeam)
        assertEquals("Все", viewModel.selectedStatus)
    }

    // ==================== ReportsViewModel Additional Edge Cases ====================

    @Test
    fun `ReportsViewModel loads reports on init`() = runTest {
        val reports = listOf(
            sampleReportItem(streamName = "Stream A"),
            sampleReportItem(streamName = "Stream B")
        )
        val viewModel = ReportsViewModel(
            repository = FakeReportRepository(reports),
            userInfoHolder = UserInfoHolder(),
            httpClient = testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        assertTrue(viewModel.state is ReportsState.Success)
        assertEquals(2, (viewModel.state as ReportsState.Success).reports.size)
    }

    @Test
    fun `ReportsViewModel setStreamFilter reloads reports`() = runTest {
        var capturedStream: String? = null
        val repository = object : ReportRepository {
            override suspend fun getReports(
                trackerUsername: String?, streamName: String?,
                page: Int, size: Int, showInactive: Boolean
            ): Result<List<ReportItem>> {
                capturedStream = streamName
                return Result.success(emptyList())
            }
            override suspend fun downloadReportsExcel(
                trackerUsername: String?, streamName: String?,
                page: Int, size: Int, showInactive: Boolean
            ) = Result.success(byteArrayOf())
        }
        val viewModel = ReportsViewModel(
            repository, UserInfoHolder(),
            testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        viewModel.setStreamFilter("Stream A")
        advanceUntilIdle()

        assertEquals("Stream A", capturedStream)
    }

    @Test
    fun `ReportsViewModel setTrackerFilter passes null for Все`() = runTest {
        var capturedTracker: String? = "not-null"
        val repository = object : ReportRepository {
            override suspend fun getReports(
                trackerUsername: String?, streamName: String?,
                page: Int, size: Int, showInactive: Boolean
            ): Result<List<ReportItem>> {
                capturedTracker = trackerUsername
                return Result.success(emptyList())
            }
            override suspend fun downloadReportsExcel(
                trackerUsername: String?, streamName: String?,
                page: Int, size: Int, showInactive: Boolean
            ) = Result.success(byteArrayOf())
        }
        val viewModel = ReportsViewModel(
            repository, UserInfoHolder(),
            testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        // Default is "Все", so loadReports should pass null
        assertNull(capturedTracker)
    }

    @Test
    fun `ReportsViewModel setStreamFilter passes null for Все`() = runTest {
        var capturedStream: String? = "not-null"
        val repository = object : ReportRepository {
            override suspend fun getReports(
                trackerUsername: String?, streamName: String?,
                page: Int, size: Int, showInactive: Boolean
            ): Result<List<ReportItem>> {
                capturedStream = streamName
                return Result.success(emptyList())
            }
            override suspend fun downloadReportsExcel(
                trackerUsername: String?, streamName: String?,
                page: Int, size: Int, showInactive: Boolean
            ) = Result.success(byteArrayOf())
        }
        val viewModel = ReportsViewModel(
            repository, UserInfoHolder(),
            testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        // Default is "Все", so loadReports should pass null
        assertNull(capturedStream)
    }

    @Test
    fun `ReportsViewModel toggleShowInactive toggles correctly`() = runTest {
        val viewModel = ReportsViewModel(
            FakeReportRepository(emptyList()),
            UserInfoHolder(),
            testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        assertFalse(viewModel.showInactive)

        viewModel.toggleShowInactive()
        advanceUntilIdle()

        assertTrue(viewModel.showInactive)

        viewModel.toggleShowInactive()
        advanceUntilIdle()

        assertFalse(viewModel.showInactive)
    }

    @Test
    fun `ReportsViewModel prepareReportExcel builds filename for specific stream`() = runTest {
        val viewModel = ReportsViewModel(
            FakeReportRepository(emptyList(), excelBytes = byteArrayOf(5)),
            UserInfoHolder(),
            testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        viewModel.setStreamFilter("Stream Alpha")
        advanceUntilIdle()

        val result = viewModel.prepareReportExcel()
        assertTrue(result.isSuccess)
        assertEquals("Отчёт_команды_Stream Alpha.xlsx", result.getOrThrow().first)
    }

    @Test
    fun `ReportsViewModel prepareReportExcel failure`() = runTest {
        val viewModel = ReportsViewModel(
            FakeReportRepository(emptyList(), excelResult = Result.failure(Exception("fail"))),
            UserInfoHolder(),
            testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        val result = viewModel.prepareReportExcel()
        assertTrue(result.isFailure)
    }

    @Test
    fun `ReportsViewModel updateFilterOptions populates streams`() = runTest {
        val reports = listOf(
            sampleReportItem(streamName = "Stream B"),
            sampleReportItem(streamName = "Stream A"),
            sampleReportItem(streamName = "Stream B")
        )
        val viewModel = ReportsViewModel(
            FakeReportRepository(reports),
            UserInfoHolder(),
            testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        assertTrue(viewModel.availableStreams.contains("Все"))
        assertTrue(viewModel.availableStreams.contains("Stream A"))
        assertTrue(viewModel.availableStreams.contains("Stream B"))
        // Streams should be sorted
        val всеIndex = viewModel.availableStreams.indexOf("Все")
        val streamAIndex = viewModel.availableStreams.indexOf("Stream A")
        assertTrue(всеIndex < streamAIndex)
    }

    @Test
    fun `ReportsViewModel TrackerInfo data class`() {
        val info = ReportsViewModel.TrackerInfo("alice", "Alice Smith")
        assertEquals("alice", info.username)
        assertEquals("Alice Smith", info.fullName)
    }

    @Test
    fun `TrackerDto and TrackersPageDto serialization`() {
        val page = TrackersPageDto(content = listOf(TrackerDto("1", "alice", "Alice")))
        val json = kotlinx.serialization.json.Json.encodeToString(TrackersPageDto.serializer(), page)
        assertTrue(json.contains("Alice"))
        assertTrue(json.contains("alice"))
    }

    // ==================== Helpers ====================

    private fun sampleMeetingReport(
        teamName: String = "Team",
        trackerFullName: String = "Tracker",
        teamStatus: String = "OK"
    ) = StreamMeetingReportItem(
        teamName = teamName,
        trackerFullName = trackerFullName,
        teamStatus = teamStatus,
        status = teamStatus
    )

    private fun sampleReportItem(streamName: String = "Stream") = ReportItem(
        streamName = streamName,
        startDate = "2024-01-01",
        endDate = "2024-12-31",
        teamCardName = "Team",
        username = "tracker",
        averageTeamGrade = 4.0,
        averageUserGrade = 3.0,
        meetingsCountPlan = 10,
        meetingsCountFact = 8,
        ntiMarkets = listOf("M"),
        readinessLevel = "5"
    )

    private class FakeStreamMeetingReportRepository(
        private val items: List<StreamMeetingReportItem> = emptyList(),
        private val getResult: Result<List<StreamMeetingReportItem>>? = null,
        private val excelBytes: ByteArray = byteArrayOf()
    ) : StreamMeetingReportRepository {
        override suspend fun getReportsByStream(
            streamId: String, page: Int, size: Int, sort: List<String>
        ): Result<List<StreamMeetingReportItem>> = getResult ?: Result.success(items)

        override suspend fun downloadReportsExcel(
            streamId: String, trackerFilter: String?, teamFilter: String?,
            statusFilter: String?, page: Int, size: Int, sort: List<String>
        ): Result<ByteArray> = Result.success(excelBytes)
    }

    private class FakeReportRepository(
        private val reports: List<ReportItem> = emptyList(),
        private val getResult: Result<List<ReportItem>>? = null,
        private val excelBytes: ByteArray = byteArrayOf(),
        private val excelResult: Result<ByteArray>? = null
    ) : ReportRepository {
        override suspend fun getReports(
            trackerUsername: String?, streamName: String?,
            page: Int, size: Int, showInactive: Boolean
        ): Result<List<ReportItem>> = getResult ?: Result.success(reports)

        override suspend fun downloadReportsExcel(
            trackerUsername: String?, streamName: String?,
            page: Int, size: Int, showInactive: Boolean
        ): Result<ByteArray> = excelResult ?: Result.success(excelBytes)
    }
}