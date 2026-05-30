package com.example.track_me_mobile.features.users.presentation

import com.example.track_me_mobile.features.users.data.model.PagedResponse
import com.example.track_me_mobile.features.users.data.model.PageInfo
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerAdminListViewModelBigTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── TrackerListViewModel: init / loadUsers ───────────────────────────────

    @Test
    fun `TrackerListViewModel init loads trackers`() = runTest {
        val repo = FakeUsersRepository(
            getTrackersResult = Result.success(pagedResponse(listOf(
                userDto("1", "alice", "Alice", true)
            )))
        )
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
        assertEquals(1, vm.state.value.users.size)
        assertEquals("Alice", vm.state.value.users.first().fullName)
    }

    @Test
    fun `TrackerListViewModel init failure sets error`() = runTest {
        val repo = FakeUsersRepository(
            getTrackersResult = Result.failure(Exception("fail"))
        )
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertEquals("Не удалось загрузить трекеров", vm.state.value.error)
    }

    @Test
    fun `TrackerListViewModel loadUsers reloads`() = runTest {
        var callCount = 0
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                callCount++
                return if (callCount == 1) {
                    Result.success(pagedResponse(listOf(userDto("1", "a", "First", true))))
                } else {
                    Result.success(pagedResponse(listOf(userDto("2", "b", "Second", true))))
                }
            }
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()
        assertEquals("First", vm.state.value.users.first().fullName)

        vm.loadUsers()
        advanceUntilIdle()
        assertEquals("Second", vm.state.value.users.first().fullName)
    }

    @Test
    fun `TrackerListViewModel loadUsers with empty list`() = runTest {
        val repo = FakeUsersRepository(
            getTrackersResult = Result.success(pagedResponse(emptyList()))
        )
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        assertTrue(vm.state.value.users.isEmpty())
    }

    @Test
    fun `TrackerListViewModel loadUsers with multiple pages`() = runTest {
        val users = (1..50).map { userDto(it.toString(), "user$it", "User $it", true) }
        val repo = FakeUsersRepository(
            getTrackersResult = Result.success(pagedResponse(users))
        )
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        assertEquals(50, vm.state.value.users.size)
    }

    // ─── TrackerListViewModel: search ─────────────────────────────────────────

    @Test
    fun `TrackerListViewModel onSearchQueryChanged filters by username`() = runTest {
        val users = listOf(
            userDto("1", "alice", "Alice Smith", true),
            userDto("2", "bob", "Bob", true)
        )
        val repo = FakeUsersRepository(
            getTrackersResult = Result.success(pagedResponse(users))
        )
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChanged("bob")
        assertEquals(1, vm.state.value.users.size)
        assertEquals("Bob", vm.state.value.users.first().fullName)
    }

    @Test
    fun `TrackerListViewModel onSearchQueryChanged filters by fullName`() = runTest {
        val users = listOf(
            userDto("1", "alice", "Alice Smith", true),
            userDto("2", "bob", "Bob Johnson", true)
        )
        val repo = FakeUsersRepository(
            getTrackersResult = Result.success(pagedResponse(users))
        )
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChanged("Smith")
        assertEquals(1, vm.state.value.users.size)
        assertEquals("Alice Smith", vm.state.value.users.first().fullName)
    }

    @Test
    fun `TrackerListViewModel onSearchQueryChanged with empty query shows all`() = runTest {
        val users = listOf(
            userDto("1", "alice", "Alice", true),
            userDto("2", "bob", "Bob", true)
        )
        val repo = FakeUsersRepository(
            getTrackersResult = Result.success(pagedResponse(users))
        )
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChanged("bob")
        assertEquals(1, vm.state.value.users.size)

        vm.onSearchQueryChanged("")
        assertEquals(2, vm.state.value.users.size)
    }

    @Test
    fun `TrackerListViewModel onSearchQueryChanged case insensitive`() = runTest {
        val users = listOf(
            userDto("1", "ALICE", "Alice", true),
            userDto("2", "bob", "Bob", true)
        )
        val repo = FakeUsersRepository(
            getTrackersResult = Result.success(pagedResponse(users))
        )
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChanged("alice")
        assertEquals(1, vm.state.value.users.size)
    }

    @Test
    fun `TrackerListViewModel onSearchQueryChanged no match`() = runTest {
        val users = listOf(
            userDto("1", "alice", "Alice", true),
            userDto("2", "bob", "Bob", true)
        )
        val repo = FakeUsersRepository(
            getTrackersResult = Result.success(pagedResponse(users))
        )
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChanged("nonexistent")
        assertTrue(vm.state.value.users.isEmpty())
    }

    // ─── TrackerListViewModel: toggleBlockedFilter ────────────────────────────

    @Test
    fun `TrackerListViewModel toggleBlockedFilter flips flag and reloads`() = runTest {
        var capturedShowBlocked = false
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                capturedShowBlocked = sb
                return Result.success(pagedResponse(emptyList()))
            }
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()
        assertFalse(capturedShowBlocked)

        vm.toggleBlockedFilter()
        advanceUntilIdle()
        assertTrue(capturedShowBlocked)
    }

    @Test
    fun `TrackerListViewModel toggleBlockedFilter twice`() = runTest {
        var capturedShowBlocked = false
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                capturedShowBlocked = sb
                return Result.success(pagedResponse(emptyList()))
            }
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        vm.toggleBlockedFilter()
        advanceUntilIdle()
        assertTrue(capturedShowBlocked)

        vm.toggleBlockedFilter()
        advanceUntilIdle()
        assertFalse(capturedShowBlocked)
    }

    // ─── TrackerListViewModel: confirmUser ────────────────────────────────────

    @Test
    fun `TrackerListViewModel confirmUser success removes user`() = runTest {
        var enabledUsername: String? = null
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                return Result.success(pagedResponse(listOf(
                    userDto("1", "alice", "Alice", true)
                )))
            }
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String): Result<Unit> {
                enabledUsername = username
                return Result.success(Unit)
            }
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        vm.confirmUser("alice")
        advanceUntilIdle()

        assertEquals("alice", enabledUsername)
        assertTrue(vm.state.value.users.isEmpty())
    }

    @Test
    fun `TrackerListViewModel confirmUser failure keeps user`() = runTest {
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                return Result.success(pagedResponse(listOf(
                    userDto("1", "alice", "Alice", true)
                )))
            }
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String): Result<Unit> {
                return Result.failure(Exception("enable failed"))
            }
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        vm.confirmUser("alice")
        advanceUntilIdle()

        assertEquals(1, vm.state.value.users.size)
    }

    // ─── TrackerListViewModel: deleteUser ─────────────────────────────────────

    @Test
    fun `TrackerListViewModel deleteUser success removes user`() = runTest {
        var disabledUsername: String? = null
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                return Result.success(pagedResponse(listOf(
                    userDto("1", "alice", "Alice", true)
                )))
            }
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> {
                disabledUsername = username
                return Result.success(Unit)
            }
        }
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        vm.deleteUser("alice")
        advanceUntilIdle()

        assertEquals("alice", disabledUsername)
        assertTrue(vm.state.value.users.isEmpty())
    }

    @Test
    fun `TrackerListViewModel deleteUser failure keeps user`() = runTest {
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                return Result.success(pagedResponse(listOf(
                    userDto("1", "alice", "Alice", true)
                )))
            }
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> {
                return Result.failure(Exception("disable failed"))
            }
        }
        val vm = TrackerListViewModel(repo)
        advanceUntilIdle()

        vm.deleteUser("alice")
        advanceUntilIdle()

        assertEquals(1, vm.state.value.users.size)
    }

    // ─── AdminListViewModel: init / loadUsers ─────────────────────────────────

    @Test
    fun `AdminListViewModel init loads admins`() = runTest {
        val repo = FakeUsersRepository(
            getAdministratorsResult = Result.success(pagedResponse(listOf(
                userDto("1", "admin1", "Admin One", true)
            )))
        )
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
        assertEquals(1, vm.state.value.users.size)
        assertEquals("Admin One", vm.state.value.users.first().fullName)
    }

    @Test
    fun `AdminListViewModel init failure sets error`() = runTest {
        val repo = FakeUsersRepository(
            getAdministratorsResult = Result.failure(Exception("fail"))
        )
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertEquals("Не удалось загрузить администраторов", vm.state.value.error)
    }

    @Test
    fun `AdminListViewModel loadUsers reloads`() = runTest {
        var callCount = 0
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                callCount++
                return if (callCount == 1) {
                    Result.success(pagedResponse(listOf(userDto("1", "a", "First", true))))
                } else {
                    Result.success(pagedResponse(listOf(userDto("2", "b", "Second", true))))
                }
            }
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()
        assertEquals("First", vm.state.value.users.first().fullName)

        vm.loadUsers()
        advanceUntilIdle()
        assertEquals("Second", vm.state.value.users.first().fullName)
    }

    @Test
    fun `AdminListViewModel loadUsers with empty list`() = runTest {
        val repo = FakeUsersRepository(
            getAdministratorsResult = Result.success(pagedResponse(emptyList()))
        )
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        assertTrue(vm.state.value.users.isEmpty())
    }

    // ─── AdminListViewModel: search ───────────────────────────────────────────

    @Test
    fun `AdminListViewModel onSearchQueryChanged filters by username`() = runTest {
        val users = listOf(
            userDto("1", "admin1", "Admin One", true),
            userDto("2", "super", "Super Admin", true)
        )
        val repo = FakeUsersRepository(
            getAdministratorsResult = Result.success(pagedResponse(users))
        )
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChanged("super")
        assertEquals(1, vm.state.value.users.size)
        assertEquals("Super Admin", vm.state.value.users.first().fullName)
    }

    @Test
    fun `AdminListViewModel onSearchQueryChanged filters by fullName`() = runTest {
        val users = listOf(
            userDto("1", "a1", "Alpha Admin", true),
            userDto("2", "a2", "Beta Admin", true)
        )
        val repo = FakeUsersRepository(
            getAdministratorsResult = Result.success(pagedResponse(users))
        )
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChanged("Alpha")
        assertEquals(1, vm.state.value.users.size)
    }

    @Test
    fun `AdminListViewModel onSearchQueryChanged with empty query shows all`() = runTest {
        val users = listOf(
            userDto("1", "a1", "Admin One", true),
            userDto("2", "a2", "Admin Two", true)
        )
        val repo = FakeUsersRepository(
            getAdministratorsResult = Result.success(pagedResponse(users))
        )
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChanged("One")
        assertEquals(1, vm.state.value.users.size)

        vm.onSearchQueryChanged("")
        assertEquals(2, vm.state.value.users.size)
    }

    @Test
    fun `AdminListViewModel onSearchQueryChanged case insensitive`() = runTest {
        val users = listOf(
            userDto("1", "ADMIN", "Admin", true),
            userDto("2", "root", "Root", true)
        )
        val repo = FakeUsersRepository(
            getAdministratorsResult = Result.success(pagedResponse(users))
        )
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        vm.onSearchQueryChanged("admin")
        assertEquals(1, vm.state.value.users.size)
    }

    // ─── AdminListViewModel: toggleBlockedFilter ──────────────────────────────

    @Test
    fun `AdminListViewModel toggleBlockedFilter flips flag and reloads`() = runTest {
        var capturedShowBlocked = false
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                capturedShowBlocked = sb
                return Result.success(pagedResponse(emptyList()))
            }
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()
        assertFalse(capturedShowBlocked)

        vm.toggleBlockedFilter()
        advanceUntilIdle()
        assertTrue(capturedShowBlocked)
    }

    @Test
    fun `AdminListViewModel toggleBlockedFilter twice`() = runTest {
        var capturedShowBlocked = false
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                capturedShowBlocked = sb
                return Result.success(pagedResponse(emptyList()))
            }
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        vm.toggleBlockedFilter()
        advanceUntilIdle()
        assertTrue(capturedShowBlocked)

        vm.toggleBlockedFilter()
        advanceUntilIdle()
        assertFalse(capturedShowBlocked)
    }

    // ─── AdminListViewModel: confirmUser ──────────────────────────────────────

    @Test
    fun `AdminListViewModel confirmUser success removes user`() = runTest {
        var enabledUsername: String? = null
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                return Result.success(pagedResponse(listOf(
                    userDto("1", "admin1", "Admin One", true)
                )))
            }
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String): Result<Unit> {
                enabledUsername = username
                return Result.success(Unit)
            }
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        vm.confirmUser("admin1")
        advanceUntilIdle()

        assertEquals("admin1", enabledUsername)
        assertTrue(vm.state.value.users.isEmpty())
    }

    @Test
    fun `AdminListViewModel confirmUser failure keeps user`() = runTest {
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                return Result.success(pagedResponse(listOf(
                    userDto("1", "admin1", "Admin One", true)
                )))
            }
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String): Result<Unit> {
                return Result.failure(Exception("enable failed"))
            }
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        vm.confirmUser("admin1")
        advanceUntilIdle()

        assertEquals(1, vm.state.value.users.size)
    }

    // ─── AdminListViewModel: deleteUser ───────────────────────────────────────

    @Test
    fun `AdminListViewModel deleteUser success removes user`() = runTest {
        var disabledUsername: String? = null
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                return Result.success(pagedResponse(listOf(
                    userDto("1", "admin1", "Admin One", true)
                )))
            }
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> {
                disabledUsername = username
                return Result.success(Unit)
            }
        }
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        vm.deleteUser("admin1")
        advanceUntilIdle()

        assertEquals("admin1", disabledUsername)
        assertTrue(vm.state.value.users.isEmpty())
    }

    @Test
    fun `AdminListViewModel deleteUser failure keeps user`() = runTest {
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> {
                return Result.success(pagedResponse(listOf(
                    userDto("1", "admin1", "Admin One", true)
                )))
            }
            override suspend fun getUserInfo(username: String) = Result.failure<UserDto>(Exception("n/a"))
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> {
                return Result.failure(Exception("disable failed"))
            }
        }
        val vm = AdminListViewModel(repo)
        advanceUntilIdle()

        vm.deleteUser("admin1")
        advanceUntilIdle()

        assertEquals(1, vm.state.value.users.size)
    }

    // ─── Helper methods ───────────────────────────────────────────────────────

    private fun userDto(id: String, username: String, fullName: String, enabled: Boolean) = UserDto(
        id = id, username = username, roles = listOf("TRACKER"),
        fullName = fullName, email = "$username@example.com",
        phoneNumber = "+70000000000", avatarUrl = null, enabled = enabled
    )

    private fun pagedResponse(content: List<UserDto>) = PagedResponse(
        content = content,
        page = PageInfo(size = content.size, number = 0, totalElements = content.size.toLong(), totalPages = 1)
    )

    private class FakeUsersRepository(
        private val getTrackersResult: Result<PagedResponse<UserDto>> = Result.failure(Exception("not configured")),
        private val getAdministratorsResult: Result<PagedResponse<UserDto>> = Result.failure(Exception("not configured"))
    ) : UsersRepository {
        override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> = getTrackersResult
        override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean): Result<PagedResponse<UserDto>> = getAdministratorsResult
        override suspend fun getUserInfo(username: String): Result<UserDto> = Result.failure(Exception("n/a"))
        override suspend fun enableUser(username: String): Result<Unit> = Result.success(Unit)
        override suspend fun disableUser(username: String): Result<Unit> = Result.success(Unit)
    }
}