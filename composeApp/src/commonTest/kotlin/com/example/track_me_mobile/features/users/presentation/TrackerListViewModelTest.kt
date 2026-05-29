package com.example.track_me_mobile.features.users.presentation

import com.example.track_me_mobile.features.users.data.model.PagedResponse
import com.example.track_me_mobile.features.users.data.model.PageInfo
import com.example.track_me_mobile.features.users.data.model.UserDto
import com.example.track_me_mobile.features.users.domain.UsersRepository
import com.example.track_me_mobile.features.users.domain.models.TrackerUser
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
class TrackerListViewModelTest {

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
    fun `loadUsers success populates state`() = runTest {
        val repository = FakeUsersRepository(
            getTrackersResult = Result.success(
                PagedResponse(
                    content = listOf(
                        UserDto(
                            id = "1",
                            username = "alice",
                            roles = listOf("TRACKER"),
                            fullName = "Alice",
                            email = "alice@example.com",
                            phoneNumber = "+70000000001",
                            avatarUrl = null,
                            enabled = true
                        )
                    ),
                    page = PageInfo(size = 1, number = 0, totalElements = 1, totalPages = 1)
                )
            )
        )
        val viewModel = TrackerListViewModel(repository)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.users.size)
        assertEquals("Alice", viewModel.state.value.users.first().fullName)
    }

    @Test
    fun `onSearchQueryChanged filters tracker list`() = runTest {
        val repository = FakeUsersRepository(
            getTrackersResult = Result.success(
                PagedResponse(
                    content = listOf(
                        UserDto(
                            id = "1",
                            username = "alice",
                            roles = listOf("TRACKER"),
                            fullName = "Alice Smith",
                            email = "alice@example.com",
                            phoneNumber = "+70000000001",
                            avatarUrl = null,
                            enabled = true
                        ),
                        UserDto(
                            id = "2",
                            username = "bob",
                            roles = listOf("TRACKER"),
                            fullName = "Bob",
                            email = "bob@example.com",
                            phoneNumber = "+70000000002",
                            avatarUrl = null,
                            enabled = true
                        )
                    ),
                    page = PageInfo(size = 2, number = 0, totalElements = 2, totalPages = 1)
                )
            )
        )
        val viewModel = TrackerListViewModel(repository)

        advanceUntilIdle()
        viewModel.onSearchQueryChanged("bob")

        assertEquals(1, viewModel.state.value.users.size)
        assertEquals("Bob", viewModel.state.value.users.first().fullName)
    }

    @Test
    fun `confirmUser success removes user from list`() = runTest {
        val repository = FakeUsersRepository(
            getTrackersResult = Result.success(
                PagedResponse(
                    content = listOf(
                        UserDto(
                            id = "1",
                            username = "alice",
                            roles = listOf("TRACKER"),
                            fullName = "Alice",
                            email = "alice@example.com",
                            phoneNumber = "+70000000001",
                            avatarUrl = null,
                            enabled = true
                        )
                    ),
                    page = PageInfo(size = 1, number = 0, totalElements = 1, totalPages = 1)
                )
            ),
            enableResult = Result.success(Unit)
        )
        val viewModel = TrackerListViewModel(repository)

        advanceUntilIdle()
        viewModel.confirmUser("alice")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.users.isEmpty())
    }

    private class FakeUsersRepository(
        private val getTrackersResult: Result<PagedResponse<UserDto>> = Result.failure(Exception("not configured")),
        private val getAdministratorsResult: Result<PagedResponse<UserDto>> = Result.failure(Exception("not configured")),
        private val getUserInfoResult: Result<com.example.track_me_mobile.features.users.data.model.UserDto> = Result.failure(Exception("not configured")),
        private val enableResult: Result<Unit> = Result.success(Unit),
        private val disableResult: Result<Unit> = Result.success(Unit)
    ) : UsersRepository {
        override suspend fun getTrackers(
            page: Int,
            size: Int,
            sort: List<String>,
            showBlocked: Boolean
        ): Result<PagedResponse<UserDto>> = getTrackersResult

        override suspend fun getAdministrators(
            page: Int,
            size: Int,
            sort: List<String>,
            showBlocked: Boolean
        ): Result<PagedResponse<UserDto>> = getAdministratorsResult

        override suspend fun getUserInfo(username: String): Result<com.example.track_me_mobile.features.users.data.model.UserDto> = getUserInfoResult
        override suspend fun enableUser(username: String): Result<Unit> = enableResult
        override suspend fun disableUser(username: String): Result<Unit> = disableResult
    }
}
