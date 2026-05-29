package com.example.track_me_mobile.features.users.presentation.components

import com.example.track_me_mobile.features.users.data.model.PagedResponse
import com.example.track_me_mobile.features.users.data.model.PageInfo
import com.example.track_me_mobile.features.users.data.model.UserDto
import com.example.track_me_mobile.features.users.domain.UsersRepository
import com.example.track_me_mobile.features.users.presentation.AdminListViewModel
import com.example.track_me_mobile.features.users.presentation.TrackerListViewModel
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
class UsersComponentsUiTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== TrackerListViewModel Tests ====================

    @Test
    fun `TrackerListViewModel loads users on init`() = runTest {
        val users = listOf(
            UserDto("1", "alice", listOf("TRACKER"), "Alice", "a@a.com", null, null, true),
            UserDto("2", "bob", listOf("TRACKER"), "Bob", "b@b.com", null, null, false)
        )
        val viewModel = TrackerListViewModel(FakeUsersRepository(trackers = users))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        assertEquals(2, viewModel.state.value.users.size)
        assertEquals("Alice", viewModel.state.value.users[0].fullName)
    }

    @Test
    fun `TrackerListViewModel search filters by fullName`() = runTest {
        val users = listOf(
            UserDto("1", "alice", listOf("TRACKER"), "Alice Wonderland", "a@a.com", null, null, true),
            UserDto("2", "bob", listOf("TRACKER"), "Bob Builder", "b@b.com", null, null, true)
        )
        val viewModel = TrackerListViewModel(FakeUsersRepository(trackers = users))
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Alice")
        assertEquals(1, viewModel.state.value.users.size)
        assertEquals("Alice Wonderland", viewModel.state.value.users.first().fullName)
    }

    @Test
    fun `TrackerListViewModel search filters by telegramNick`() = runTest {
        val users = listOf(
            UserDto("1", "alice_wonder", listOf("TRACKER"), "Alice", "a@a.com", null, null, true),
            UserDto("2", "bob_build", listOf("TRACKER"), "Bob", "b@b.com", null, null, true)
        )
        val viewModel = TrackerListViewModel(FakeUsersRepository(trackers = users))
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("bob_build")
        assertEquals(1, viewModel.state.value.users.size)
        assertEquals("Bob", viewModel.state.value.users.first().fullName)
    }

    @Test
    fun `TrackerListViewModel empty query returns all users`() = runTest {
        val users = listOf(
            UserDto("1", "alice", listOf("TRACKER"), "Alice", "a@a.com", null, null, true),
            UserDto("2", "bob", listOf("TRACKER"), "Bob", "b@b.com", null, null, true)
        )
        val viewModel = TrackerListViewModel(FakeUsersRepository(trackers = users))
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Alice")
        assertEquals(1, viewModel.state.value.users.size)

        viewModel.onSearchQueryChanged("")
        assertEquals(2, viewModel.state.value.users.size)
    }

    @Test
    fun `TrackerListViewModel case insensitive search`() = runTest {
        val users = listOf(
            UserDto("1", "alice", listOf("TRACKER"), "Alice Wonderland", "a@a.com", null, null, true)
        )
        val viewModel = TrackerListViewModel(FakeUsersRepository(trackers = users))
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("alice wonderland")
        assertEquals(1, viewModel.state.value.users.size)

        viewModel.onSearchQueryChanged("ALICE")
        assertEquals(1, viewModel.state.value.users.size)
    }

    @Test
    fun `TrackerListViewModel toggleBlockedFilter reloads with showBlocked`() = runTest {
        var capturedShowBlocked = false
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                capturedShowBlocked = showBlocked
                return Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            }
            override suspend fun getAdministrators(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
            override suspend fun enableUser(username: String): Result<Unit> = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showBlocked)

        viewModel.toggleBlockedFilter()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showBlocked)
        assertTrue(capturedShowBlocked)
    }

    @Test
    fun `TrackerListViewModel confirmUser enables and removes from list`() = runTest {
        var enabledUsername: String? = null
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                return Result.success(PagedResponse(
                    listOf(UserDto("1", "alice", listOf("TRACKER"), "Alice", "a@a.com", null, null, false)),
                    PageInfo(100, 0, 1, 1)
                ))
            }
            override suspend fun getAdministrators(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
            override suspend fun enableUser(username: String): Result<Unit> {
                enabledUsername = username
                return Result.success(Unit)
            }
            override suspend fun disableUser(username: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.users.size)

        viewModel.confirmUser("alice")
        advanceUntilIdle()

        assertEquals("alice", enabledUsername)
        assertEquals(0, viewModel.state.value.users.size)
    }

    @Test
    fun `TrackerListViewModel confirmUser failure sets error`() = runTest {
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                return Result.success(PagedResponse(
                    listOf(UserDto("1", "alice", listOf("TRACKER"), "Alice", "a@a.com", null, null, false)),
                    PageInfo(100, 0, 1, 1)
                ))
            }
            override suspend fun getAdministrators(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
            override suspend fun enableUser(username: String): Result<Unit> = Result.failure(Exception("API error"))
            override suspend fun disableUser(username: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        viewModel.confirmUser("alice")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `TrackerListViewModel deleteUser disables and removes from list`() = runTest {
        var disabledUsername: String? = null
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                return Result.success(PagedResponse(
                    listOf(UserDto("1", "alice", listOf("TRACKER"), "Alice", "a@a.com", null, null, true)),
                    PageInfo(100, 0, 1, 1)
                ))
            }
            override suspend fun getAdministrators(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
            override suspend fun enableUser(username: String): Result<Unit> = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> {
                disabledUsername = username
                return Result.success(Unit)
            }
        }
        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.users.size)

        viewModel.deleteUser("alice")
        advanceUntilIdle()

        assertEquals("alice", disabledUsername)
        assertEquals(0, viewModel.state.value.users.size)
    }

    @Test
    fun `TrackerListViewModel deleteUser failure sets error`() = runTest {
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                return Result.success(PagedResponse(
                    listOf(UserDto("1", "alice", listOf("TRACKER"), "Alice", "a@a.com", null, null, true)),
                    PageInfo(100, 0, 1, 1)
                ))
            }
            override suspend fun getAdministrators(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
            override suspend fun enableUser(username: String): Result<Unit> = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> = Result.failure(Exception("API error"))
        }
        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        viewModel.deleteUser("alice")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `TrackerListViewModel load failure sets error`() = runTest {
        val viewModel = TrackerListViewModel(FakeUsersRepository(trackersResult = Result.failure(Exception("fail"))))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.users.isEmpty())
    }

    @Test
    fun `TrackerListViewModel shows loading during initial load`() = runTest {
        val viewModel = TrackerListViewModel(FakeUsersRepository(trackers = listOf(
            UserDto("1", "alice", listOf("TRACKER"), "Alice", "a@a.com", null, null, true)
        )))
        // Before advanceUntilIdle, the coroutine hasn't completed yet
        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    // ==================== AdminListViewModel Tests ====================

    @Test
    fun `AdminListViewModel loads users on init`() = runTest {
        val users = listOf(
            UserDto("1", "admin1", listOf("ADMIN"), "Admin One", "a@a.com", null, null, true),
            UserDto("2", "admin2", listOf("ADMIN"), "Admin Two", "b@b.com", null, null, false)
        )
        val viewModel = AdminListViewModel(FakeUsersRepository(admins = users))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        assertEquals(2, viewModel.state.value.users.size)
        assertEquals("Admin One", viewModel.state.value.users[0].fullName)
    }

    @Test
    fun `AdminListViewModel search filters by fullName`() = runTest {
        val users = listOf(
            UserDto("1", "admin1", listOf("ADMIN"), "Admin One", "a@a.com", null, null, true),
            UserDto("2", "admin2", listOf("ADMIN"), "Admin Two", "b@b.com", null, null, true)
        )
        val viewModel = AdminListViewModel(FakeUsersRepository(admins = users))
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Admin One")
        assertEquals(1, viewModel.state.value.users.size)
    }

    @Test
    fun `AdminListViewModel empty query returns all users`() = runTest {
        val users = listOf(
            UserDto("1", "admin1", listOf("ADMIN"), "Admin One", "a@a.com", null, null, true),
            UserDto("2", "admin2", listOf("ADMIN"), "Admin Two", "b@b.com", null, null, true)
        )
        val viewModel = AdminListViewModel(FakeUsersRepository(admins = users))
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Admin One")
        assertEquals(1, viewModel.state.value.users.size)

        viewModel.onSearchQueryChanged("")
        assertEquals(2, viewModel.state.value.users.size)
    }

    @Test
    fun `AdminListViewModel toggleBlockedFilter reloads with showBlocked`() = runTest {
        var capturedShowBlocked = false
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            override suspend fun getAdministrators(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                capturedShowBlocked = showBlocked
                return Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            }
            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
            override suspend fun enableUser(username: String): Result<Unit> = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = AdminListViewModel(repository)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showBlocked)

        viewModel.toggleBlockedFilter()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showBlocked)
        assertTrue(capturedShowBlocked)
    }

    @Test
    fun `AdminListViewModel confirmUser enables and removes from list`() = runTest {
        var enabledUsername: String? = null
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            override suspend fun getAdministrators(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                return Result.success(PagedResponse(
                    listOf(UserDto("1", "admin1", listOf("ADMIN"), "Admin", "a@a.com", null, null, false)),
                    PageInfo(100, 0, 1, 1)
                ))
            }
            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
            override suspend fun enableUser(username: String): Result<Unit> {
                enabledUsername = username
                return Result.success(Unit)
            }
            override suspend fun disableUser(username: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = AdminListViewModel(repository)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.users.size)

        viewModel.confirmUser("admin1")
        advanceUntilIdle()

        assertEquals("admin1", enabledUsername)
        assertEquals(0, viewModel.state.value.users.size)
    }

    @Test
    fun `AdminListViewModel confirmUser failure sets error`() = runTest {
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            override suspend fun getAdministrators(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                return Result.success(PagedResponse(
                    listOf(UserDto("1", "admin1", listOf("ADMIN"), "Admin", "a@a.com", null, null, false)),
                    PageInfo(100, 0, 1, 1)
                ))
            }
            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
            override suspend fun enableUser(username: String): Result<Unit> = Result.failure(Exception("API error"))
            override suspend fun disableUser(username: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = AdminListViewModel(repository)
        advanceUntilIdle()

        viewModel.confirmUser("admin1")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `AdminListViewModel deleteUser disables and removes from list`() = runTest {
        var disabledUsername: String? = null
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            override suspend fun getAdministrators(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                return Result.success(PagedResponse(
                    listOf(UserDto("1", "admin1", listOf("ADMIN"), "Admin", "a@a.com", null, null, true)),
                    PageInfo(100, 0, 1, 1)
                ))
            }
            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
            override suspend fun enableUser(username: String): Result<Unit> = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> {
                disabledUsername = username
                return Result.success(Unit)
            }
        }
        val viewModel = AdminListViewModel(repository)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.users.size)

        viewModel.deleteUser("admin1")
        advanceUntilIdle()

        assertEquals("admin1", disabledUsername)
        assertEquals(0, viewModel.state.value.users.size)
    }

    @Test
    fun `AdminListViewModel deleteUser failure sets error`() = runTest {
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> = Result.success(PagedResponse(emptyList(), PageInfo(100, 0, 0, 0)))
            override suspend fun getAdministrators(
                page: Int, size: Int, sort: List<String>, showBlocked: Boolean
            ): Result<PagedResponse<UserDto>> {
                return Result.success(PagedResponse(
                    listOf(UserDto("1", "admin1", listOf("ADMIN"), "Admin", "a@a.com", null, null, true)),
                    PageInfo(100, 0, 1, 1)
                ))
            }
            override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
            override suspend fun enableUser(username: String): Result<Unit> = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> = Result.failure(Exception("API error"))
        }
        val viewModel = AdminListViewModel(repository)
        advanceUntilIdle()

        viewModel.deleteUser("admin1")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `AdminListViewModel load failure sets error`() = runTest {
        val viewModel = AdminListViewModel(FakeUsersRepository(adminsResult = Result.failure(Exception("fail"))))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.users.isEmpty())
    }

    @Test
    fun `AdminListViewModel shows loading during initial load`() = runTest {
        val viewModel = AdminListViewModel(FakeUsersRepository(admins = listOf(
            UserDto("1", "admin1", listOf("ADMIN"), "Admin", "a@a.com", null, null, true)
        )))
        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    // ==================== Fake Repository ====================

    private class FakeUsersRepository(
        private val trackers: List<UserDto> = emptyList(),
        private val admins: List<UserDto> = emptyList(),
        private val trackersResult: Result<PagedResponse<UserDto>>? = null,
        private val adminsResult: Result<PagedResponse<UserDto>>? = null
    ) : UsersRepository {
        override suspend fun getTrackers(
            page: Int, size: Int, sort: List<String>, showBlocked: Boolean
        ): Result<PagedResponse<UserDto>> = trackersResult ?: Result.success(
            PagedResponse(trackers, PageInfo(size, page, trackers.size.toLong(), 1))
        )

        override suspend fun getAdministrators(
            page: Int, size: Int, sort: List<String>, showBlocked: Boolean
        ): Result<PagedResponse<UserDto>> = adminsResult ?: Result.success(
            PagedResponse(admins, PageInfo(size, page, admins.size.toLong(), 1))
        )

        override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception(""))
        override suspend fun enableUser(username: String): Result<Unit> = Result.success(Unit)
        override suspend fun disableUser(username: String): Result<Unit> = Result.success(Unit)
    }
}