package com.example.track_me_mobile.features.reports.presentation

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.reports.domain.ReportRepository
import com.example.track_me_mobile.features.reports.domain.StreamMeetingReportRepository
import com.example.track_me_mobile.features.reports.domain.models.ReportItem
import com.example.track_me_mobile.features.reports.domain.models.StreamMeetingReportItem
import com.example.track_me_mobile.testsupport.csrfJson
import com.example.track_me_mobile.testsupport.testHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
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
class ReportsPresentationBigTest {

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
    fun `StreamMeetingReportViewModel loads and filters by tracker`() = runTest {
        val items = listOf(
            sampleMeetingReport(teamName = "Alpha", trackerFullName = "Alice", teamStatus = "OK"),
            sampleMeetingReport(teamName = "Beta", trackerFullName = "Bob", teamStatus = "OK")
        )
        val repository = FakeStreamMeetingReportRepository(items)
        val viewModel = StreamMeetingReportViewModel(repository, "stream-1")
        advanceUntilIdle()

        assertTrue(viewModel.state is StreamMeetingReportState.Success)
        assertEquals(2, (viewModel.state as StreamMeetingReportState.Success).items.size)
        assertTrue(viewModel.availableTrackers.contains("Alice"))
        assertTrue(viewModel.availableTeams.contains("Alpha"))

        viewModel.setTrackerFilter("Alice")
        assertEquals(1, (viewModel.state as StreamMeetingReportState.Success).items.size)
        assertEquals("Alpha", (viewModel.state as StreamMeetingReportState.Success).items.first().teamName)
    }

    @Test
    fun `StreamMeetingReportViewModel filters by team and status`() = runTest {
        val items = listOf(
            sampleMeetingReport(teamName = "Alpha", trackerFullName = "Alice", teamStatus = "OK"),
            sampleMeetingReport(teamName = "Alpha", trackerFullName = "Alice", teamStatus = "ISSUES")
        )
        val viewModel = StreamMeetingReportViewModel(FakeStreamMeetingReportRepository(items), "s1")
        advanceUntilIdle()

        viewModel.setStatusFilter("ISSUES")
        assertEquals(1, (viewModel.state as StreamMeetingReportState.Success).items.size)

        viewModel.setTeamFilter("Alpha")
        viewModel.setStatusFilter("Все")
        assertEquals(2, (viewModel.state as StreamMeetingReportState.Success).items.size)
    }

    @Test
    fun `StreamMeetingReportViewModel uses trackerName fallback when full name blank`() = runTest {
        val items = listOf(
            StreamMeetingReportItem(
                teamName = "Team",
                trackerName = "nick",
                trackerFullName = null,
                teamStatus = "OK"
            )
        )
        val viewModel = StreamMeetingReportViewModel(FakeStreamMeetingReportRepository(items), "s1")
        advanceUntilIdle()

        assertTrue(viewModel.availableTrackers.contains("nick"))
        viewModel.setTrackerFilter("nick")
        assertEquals(1, (viewModel.state as StreamMeetingReportState.Success).items.size)
    }

    @Test
    fun `StreamMeetingReportViewModel resets invalid filter selections`() = runTest {
        val items = listOf(sampleMeetingReport(teamName = "OnlyTeam", trackerFullName = "OnlyTracker", teamStatus = "OK"))
        val viewModel = StreamMeetingReportViewModel(FakeStreamMeetingReportRepository(items), "s1")
        advanceUntilIdle()

        viewModel.setTrackerFilter("MissingTracker")
        viewModel.setTeamFilter("MissingTeam")
        viewModel.setStatusFilter("MissingStatus")

        viewModel.setTrackerFilter("OnlyTracker")
        viewModel.loadReports()
        advanceUntilIdle()

        assertEquals("Все", viewModel.selectedTeam)
    }

    @Test
    fun `StreamMeetingReportViewModel load failure sets error state`() = runTest {
        val viewModel = StreamMeetingReportViewModel(
            FakeStreamMeetingReportRepository(getResult = Result.failure(Exception("fail"))),
            "s1"
        )
        advanceUntilIdle()

        assertTrue(viewModel.state is StreamMeetingReportState.Error)
    }

    @Test
    fun `StreamMeetingReportViewModel prepareExcelReport returns filename and bytes`() = runTest {
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
    fun `ReportsViewModel loads reports and updates stream filter options`() = runTest {
        val reports = listOf(
            sampleReportItem(streamName = "Stream B"),
            sampleReportItem(streamName = "Stream A")
        )
        val viewModel = ReportsViewModel(
            repository = FakeReportRepository(reports),
            userInfoHolder = UserInfoHolder(),
            httpClient = testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        assertTrue(viewModel.state is ReportsState.Success)
        assertEquals(2, (viewModel.state as ReportsState.Success).reports.size)
        assertTrue(viewModel.availableStreams.contains("Stream A"))
        assertTrue(viewModel.availableStreams.contains("Stream B"))
    }

    @Test
    fun `ReportsViewModel setTrackerFilter reloads reports`() = runTest {
        var lastTracker: String? = null
        val repository = object : ReportRepository {
            override suspend fun getReports(
                trackerUsername: String?,
                streamName: String?,
                page: Int,
                size: Int,
                showInactive: Boolean
            ): Result<List<ReportItem>> {
                lastTracker = trackerUsername
                return Result.success(emptyList())
            }

            override suspend fun downloadReportsExcel(
                trackerUsername: String?,
                streamName: String?,
                page: Int,
                size: Int,
                showInactive: Boolean
            ) = Result.success(byteArrayOf())
        }
        val viewModel = ReportsViewModel(repository, UserInfoHolder(), testHttpClient(MockEngine { respond("", HttpStatusCode.OK) }))
        advanceUntilIdle()

        viewModel.setTrackerFilter("alice")
        advanceUntilIdle()

        assertEquals("alice", lastTracker)
    }

    @Test
    fun `ReportsViewModel toggleShowInactive toggles flag and reloads`() = runTest {
        var capturedShowInactive = false
        val repository = object : ReportRepository {
            override suspend fun getReports(
                trackerUsername: String?,
                streamName: String?,
                page: Int,
                size: Int,
                showInactive: Boolean
            ): Result<List<ReportItem>> {
                capturedShowInactive = showInactive
                return Result.success(emptyList())
            }

            override suspend fun downloadReportsExcel(
                trackerUsername: String?,
                streamName: String?,
                page: Int,
                size: Int,
                showInactive: Boolean
            ) = Result.success(byteArrayOf())
        }
        val viewModel = ReportsViewModel(repository, UserInfoHolder(), testHttpClient(MockEngine { respond("", HttpStatusCode.OK) }))
        advanceUntilIdle()

        viewModel.toggleShowInactive()
        advanceUntilIdle()

        assertTrue(viewModel.showInactive)
        assertTrue(capturedShowInactive)
    }

    @Test
    fun `ReportsViewModel loadReports failure sets error`() = runTest {
        val viewModel = ReportsViewModel(
            FakeReportRepository(getResult = Result.failure(Exception("fail"))),
            UserInfoHolder(),
            testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        assertTrue(viewModel.state is ReportsState.Error)
    }

    @Test
    fun `ReportsViewModel TrackerInfo uses username when fullName is null`() {
        val dto = TrackerDto(id = "1", username = "alice", fullName = null)
        val info = ReportsViewModel.TrackerInfo(dto.username, dto.fullName ?: dto.username)
        assertEquals("alice", info.username)
        assertEquals("alice", info.fullName)
    }

    @Test
    fun `ReportsViewModel TrackerInfo preserves fullName when provided`() {
        val dto = TrackerDto(id = "1", username = "alice", fullName = "Alice Smith")
        val info = ReportsViewModel.TrackerInfo(dto.username, dto.fullName ?: dto.username)
        assertEquals("Alice Smith", info.fullName)
    }

    @Test
    fun `ReportsViewModel prepareReportExcel builds filename for all streams`() = runTest {
        val viewModel = ReportsViewModel(
            FakeReportRepository(emptyList(), excelBytes = byteArrayOf(5)),
            UserInfoHolder(),
            testHttpClient(MockEngine { respond("", HttpStatusCode.OK) })
        )
        advanceUntilIdle()

        val result = viewModel.prepareReportExcel()
        assertTrue(result.isSuccess)
        assertEquals("Отчёт_команды_все_потоки.xlsx", result.getOrThrow().first)
    }

    @Test
    fun `TrackerDto and TrackersPageDto serialize`() {
        val page = TrackersPageDto(content = listOf(TrackerDto("1", "u", "Full")))
        val json = kotlinx.serialization.json.Json.encodeToString(TrackersPageDto.serializer(), page)
        assertTrue(json.contains("Full"))
    }

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
            streamId: String,
            page: Int,
            size: Int,
            sort: List<String>
        ): Result<List<StreamMeetingReportItem>> = getResult ?: Result.success(items)

        override suspend fun downloadReportsExcel(
            streamId: String,
            trackerFilter: String?,
            teamFilter: String?,
            statusFilter: String?,
            page: Int,
            size: Int,
            sort: List<String>
        ): Result<ByteArray> = Result.success(excelBytes)
    }

    private class FakeReportRepository(
        private val reports: List<ReportItem> = emptyList(),
        private val getResult: Result<List<ReportItem>>? = null,
        private val excelBytes: ByteArray = byteArrayOf()
    ) : ReportRepository {
        override suspend fun getReports(
            trackerUsername: String?,
            streamName: String?,
            page: Int,
            size: Int,
            showInactive: Boolean
        ): Result<List<ReportItem>> = getResult ?: Result.success(reports)

        override suspend fun downloadReportsExcel(
            trackerUsername: String?,
            streamName: String?,
            page: Int,
            size: Int,
            showInactive: Boolean
        ): Result<ByteArray> = Result.success(excelBytes)
    }
}
