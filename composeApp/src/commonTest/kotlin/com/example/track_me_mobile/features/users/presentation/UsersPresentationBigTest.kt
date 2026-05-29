package com.example.track_me_mobile.features.users.presentation

import com.example.track_me_mobile.features.users.data.model.PageInfo
import com.example.track_me_mobile.features.users.data.model.PagedResponse
import com.example.track_me_mobile.features.users.data.model.UserDto
import com.example.track_me_mobile.features.users.domain.UsersRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UsersPresentationBigTest {

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
    fun `TrackerListViewModel load failure sets error`() = runTest {
        val viewModel = TrackerListViewModel(
            FakeUsersRepository(getTrackersResult = Result.failure(Exception("fail")))
        )
        advanceUntilIdle()

        assertEquals("Не удалось загрузить трекеров", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `TrackerListViewModel toggleBlockedFilter reloads with blocked flag`() = runTest {
        var capturedShowBlocked = false
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int,
                size: Int,
                sort: List<String>,
                showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                capturedShowBlocked = showBlocked
                return Result.success(emptyPaged())
            }

            override suspend fun getAdministrators(
                page: Int,
                size: Int,
                sort: List<String>,
                showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.failure(Exception("n/a"))

            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        viewModel.toggleBlockedFilter()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showBlocked)
        assertTrue(capturedShowBlocked)
    }

    @Test
    fun `TrackerListViewModel deleteUser removes user from list`() = runTest {
        val repository = FakeUsersRepository(
            getTrackersResult = Result.success(
                PagedResponse(
                    content = listOf(
                        userDto("alice", "Alice"),
                        userDto("bob", "Bob")
                    ),
                    page = PageInfo(2, 0, 2, 1)
                )
            ),
            disableResult = Result.success(Unit)
        )
        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        viewModel.deleteUser("bob")
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.users.size)
        assertEquals("Alice", viewModel.state.value.users.first().fullName)
    }

    @Test
    fun `TrackerListViewModel deleteUser failure sets error`() = runTest {
        val repository = FakeUsersRepository(
            getTrackersResult = Result.success(
                PagedResponse(listOf(userDto("alice", "Alice")), PageInfo(1, 0, 1, 1))
            ),
            disableResult = Result.failure(Exception("fail"))
        )
        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        viewModel.deleteUser("alice")
        advanceUntilIdle()

        assertEquals("Не удалось удалить пользователя", viewModel.state.value.error)
    }

    @Test
    fun `TrackerListViewModel maps id fallback and enabled flag`() = runTest {
        val repository = FakeUsersRepository(
            getTrackersResult = Result.success(
                PagedResponse(
                    content = listOf(
                        UserDto(null, "nick", listOf("TRACKER"), "Nick", "n@e.com", null, null, false)
                    ),
                    page = PageInfo(1, 0, 1, 1)
                )
            )
        )
        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        assertEquals("nick", viewModel.state.value.users.first().id)
        assertFalse(viewModel.state.value.users.first().isConfirmed)
    }

    @Test
    fun `AdminListViewModel loads administrators successfully`() = runTest {
        val repository = FakeUsersRepository(
            getAdministratorsResult = Result.success(
                PagedResponse(
                    content = listOf(userDto("admin1", "Admin One", roles = listOf("ADMIN"))),
                    page = PageInfo(1, 0, 1, 1)
                )
            )
        )
        val viewModel = AdminListViewModel(repository)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.users.size)
        assertEquals("Admin One", viewModel.state.value.users.first().fullName)
    }

    @Test
    fun `AdminListViewModel search filters admins`() = runTest {
        val repository = FakeUsersRepository(
            getAdministratorsResult = Result.success(
                PagedResponse(
                    content = listOf(
                        userDto("a1", "Alice Admin"),
                        userDto("a2", "Bob Admin")
                    ),
                    page = PageInfo(2, 0, 2, 1)
                )
            )
        )
        val viewModel = AdminListViewModel(repository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("bob")
        assertEquals(1, viewModel.state.value.users.size)
        assertEquals("Bob Admin", viewModel.state.value.users.first().fullName)
    }

    @Test
    fun `AdminListViewModel confirmUser removes user`() = runTest {
        val repository = FakeUsersRepository(
            getAdministratorsResult = Result.success(
                PagedResponse(listOf(userDto("pending", "Pending")), PageInfo(1, 0, 1, 1))
            ),
            enableResult = Result.success(Unit)
        )
        val viewModel = AdminListViewModel(repository)
        advanceUntilIdle()

        viewModel.confirmUser("pending")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.users.isEmpty())
    }

    @Test
    fun `AdminListViewModel deleteUser failure sets error`() = runTest {
        val repository = FakeUsersRepository(
            getAdministratorsResult = Result.success(
                PagedResponse(listOf(userDto("admin", "Admin")), PageInfo(1, 0, 1, 1))
            ),
            disableResult = Result.failure(Exception("fail"))
        )
        val viewModel = AdminListViewModel(repository)
        advanceUntilIdle()

        viewModel.deleteUser("admin")
        advanceUntilIdle()

        assertEquals("Не удалось удалить пользователя", viewModel.state.value.error)
    }

    @Test
    fun `AdminListViewModel load failure sets error`() = runTest {
        val viewModel = AdminListViewModel(
            FakeUsersRepository(getAdministratorsResult = Result.failure(Exception("fail")))
        )
        advanceUntilIdle()

        assertEquals("Не удалось загрузить администраторов", viewModel.state.value.error)
    }

    private fun userDto(
        username: String,
        fullName: String,
        roles: List<String> = listOf("TRACKER")
    ) = UserDto(
        id = username,
        username = username,
        roles = roles,
        fullName = fullName,
        email = "$username@example.com",
        phoneNumber = null,
        avatarUrl = null,
        enabled = true
    )

    private fun emptyPaged() = PagedResponse<UserDto>(
        content = emptyList(),
        page = PageInfo(0, 0, 0, 0)
    )

    private class FakeUsersRepository(
        private val getTrackersResult: Result<PagedResponse<UserDto>> = Result.failure(Exception("not configured")),
        private val getAdministratorsResult: Result<PagedResponse<UserDto>> = Result.failure(Exception("not configured")),
        private val enableResult: Result<Unit> = Result.success(Unit),
        private val disableResult: Result<Unit> = Result.success(Unit)
    ) : UsersRepository {
        override suspend fun getTrackers(
            page: Int,
            size: Int,
            sort: List<String>,
            showBlocked: Boolean
        ) = getTrackersResult

        override suspend fun getAdministrators(
            page: Int,
            size: Int,
            sort: List<String>,
            showBlocked: Boolean
        ) = getAdministratorsResult

        override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
        override suspend fun enableUser(username: String) = enableResult
        override suspend fun disableUser(username: String) = disableResult
    }
}
