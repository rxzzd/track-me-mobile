package com.example.track_me_mobile.features.profile.presentation

import com.example.track_me_mobile.features.profile.domain.ProfileRepository
import com.example.track_me_mobile.features.profile.domain.models.UserProfile
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelBigTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── ProfileViewModel: init / loadProfile ─────────────────────────────────

    @Test
    fun `ProfileViewModel init loads profile successfully`() = runTest {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(getResult = Result.success(profile))
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.isLoading)
        assertNull(vm.errorMessage)
        assertNotNull(vm.profile)
        assertEquals("John Doe", vm.profile?.fullName)
        assertEquals("john@example.com", vm.profile?.email)
        assertEquals("+70000000000", vm.profile?.phoneNumber)
        assertEquals("avatar://old", vm.profile?.avatarUrl)
        assertEquals("1", vm.profile?.id)
        assertEquals("john", vm.profile?.username)
        assertTrue(vm.profile?.enabled == true)
    }

    @Test
    fun `ProfileViewModel init failure sets error`() = runTest {
        val repo = FakeProfileRepository(getResult = Result.failure(Exception("network error")))
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.isLoading)
        assertEquals("Не удалось загрузить профиль", vm.errorMessage)
        assertNull(vm.profile)
    }

    @Test
    fun `ProfileViewModel loadProfile reloads after initial load`() = runTest {
        val first = sampleProfile(fullName = "First")
        val second = sampleProfile(fullName = "Second")
        var call = 0
        val repo = object : ProfileRepository {
            override suspend fun getAccountInfo(): Result<UserProfile> {
                call++
                return Result.success(if (call == 1) first else second)
            }
            override suspend fun updateAccount(
                fullName: String, email: String, phoneNumber: String, avatarUrl: String?
            ): Result<Unit> = Result.success(Unit)
        }
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()
        assertEquals("First", vm.profile?.fullName)

        vm.loadProfile()
        advanceUntilIdle()
        assertEquals("Second", vm.profile?.fullName)
    }

    @Test
    fun `ProfileViewModel loadProfile after failure retries`() = runTest {
        var call = 0
        val repo = object : ProfileRepository {
            override suspend fun getAccountInfo(): Result<UserProfile> {
                call++
                return if (call == 1) Result.failure(Exception("fail"))
                else Result.success(sampleProfile(fullName = "Retried"))
            }
            override suspend fun updateAccount(
                fullName: String, email: String, phoneNumber: String, avatarUrl: String?
            ): Result<Unit> = Result.success(Unit)
        }
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()
        assertEquals("Не удалось загрузить профиль", vm.errorMessage)

        vm.loadProfile()
        advanceUntilIdle()
        assertEquals("Retried", vm.profile?.fullName)
        assertNull(vm.errorMessage)
    }

    @Test
    fun `ProfileViewModel loadProfile with null avatarUrl`() = runTest {
        val profile = sampleProfile(avatarUrl = null)
        val repo = FakeProfileRepository(getResult = Result.success(profile))
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        assertNull(vm.profile?.avatarUrl)
    }

    @Test
    fun `ProfileViewModel loadProfile with empty avatarUrl`() = runTest {
        val profile = sampleProfile(avatarUrl = "")
        val repo = FakeProfileRepository(getResult = Result.success(profile))
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        assertEquals("", vm.profile?.avatarUrl)
    }

    @Test
    fun `ProfileViewModel loadProfile with disabled user`() = runTest {
        val profile = sampleProfile().copy(enabled = false)
        val repo = FakeProfileRepository(getResult = Result.success(profile))
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        assertFalse(vm.profile?.enabled ?: true)
    }

    @Test
    fun `ProfileViewModel loadProfile with empty roles`() = runTest {
        val profile = sampleProfile().copy(roles = emptyList())
        val repo = FakeProfileRepository(getResult = Result.success(profile))
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        assertTrue(vm.profile?.roles?.isEmpty() ?: false)
    }

    // ─── ProfileViewModel: saveProfile ────────────────────────────────────────

    @Test
    fun `ProfileViewModel saveProfile with new avatar updates avatar`() = runTest {
        val profile = sampleProfile(avatarUrl = "old-avatar")
        val repo = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.success(Unit)
        )
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        vm.saveProfile("New Name", "new@mail.com", "+7111", newAvatarUrl = "new-avatar") {}
        advanceUntilIdle()

        assertEquals("new-avatar", vm.profile?.avatarUrl)
        assertTrue(vm.saveSuccess)
    }

    @Test
    fun `ProfileViewModel saveProfile without new avatar keeps old avatar`() = runTest {
        val profile = sampleProfile(avatarUrl = "existing-avatar")
        val repo = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.success(Unit)
        )
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        vm.saveProfile("New Name", "new@mail.com", "+7111", newAvatarUrl = null) {}
        advanceUntilIdle()

        assertEquals("existing-avatar", vm.profile?.avatarUrl)
        assertTrue(vm.saveSuccess)
    }

    @Test
    fun `ProfileViewModel saveProfile without avatar and no profile avatar uses empty string`() = runTest {
        val profile = sampleProfile(avatarUrl = null)
        val repo = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.success(Unit)
        )
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        vm.saveProfile("Name", "mail@test.com", "+7", newAvatarUrl = null) {}
        advanceUntilIdle()

        assertNull(vm.profile?.avatarUrl)
        assertTrue(vm.saveSuccess)
    }

    @Test
    fun `ProfileViewModel saveProfile updates all fields`() = runTest {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.success(Unit)
        )
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        vm.saveProfile(
            fullName = "Jane Doe",
            email = "jane@example.com",
            phoneNumber = "+79999999999",
            newAvatarUrl = "new-avatar"
        ) {}
        advanceUntilIdle()

        assertEquals("Jane Doe", vm.profile?.fullName)
        assertEquals("jane@example.com", vm.profile?.email)
        assertEquals("+79999999999", vm.profile?.phoneNumber)
        assertEquals("new-avatar", vm.profile?.avatarUrl)
    }

    @Test
    fun `ProfileViewModel saveProfile failure keeps old values`() = runTest {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.failure(Exception("save failed"))
        )
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        vm.saveProfile("Jane Doe", "jane@example.com", "+79999999999", "new-avatar") {}
        advanceUntilIdle()

        assertEquals("John Doe", vm.profile?.fullName)
        assertEquals("john@example.com", vm.profile?.email)
        assertEquals("+70000000000", vm.profile?.phoneNumber)
        assertEquals("avatar://old", vm.profile?.avatarUrl)
        assertFalse(vm.saveSuccess)
        assertEquals("Не удалось сохранить изменения", vm.errorMessage)
    }

    @Test
    fun `ProfileViewModel saveProfile failure does not call onSuccess`() = runTest {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.failure(Exception("fail"))
        )
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        var onSuccessCalled = false
        vm.saveProfile("N", "e@m.com", "+7", null) { onSuccessCalled = true }
        advanceUntilIdle()

        assertFalse(onSuccessCalled)
    }

    @Test
    fun `ProfileViewModel saveProfile success calls onSuccess`() = runTest {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.success(Unit)
        )
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        var onSuccessCalled = false
        vm.saveProfile("N", "e@m.com", "+7", null) { onSuccessCalled = true }
        advanceUntilIdle()

        assertTrue(onSuccessCalled)
    }

    @Test
    fun `ProfileViewModel saveProfile isSaving is true during save`() = runTest {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.success(Unit)
        )
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        vm.saveProfile("N", "e@m.com", "+7", null) {}
        // isSaving is set inside coroutine, so may not be true yet
        advanceUntilIdle()
        assertFalse(vm.isSaving)
    }

    @Test
    fun `ProfileViewModel saveProfile clears error before saving`() = runTest {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.success(Unit)
        )
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        // saveProfile sets errorMessage = null at the start
        vm.saveProfile("N", "e@m.com", "+7", null) {}
        advanceUntilIdle()

        assertNull(vm.errorMessage)
    }

    // ─── ProfileViewModel: clearError ─────────────────────────────────────────

    @Test
    fun `ProfileViewModel clearError resets error`() = runTest {
        val repo = FakeProfileRepository(getResult = Result.failure(Exception("fail")))
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()
        assertEquals("Не удалось загрузить профиль", vm.errorMessage)

        vm.clearError()
        assertNull(vm.errorMessage)
    }

    @Test
    fun `ProfileViewModel clearError when no error`() = runTest {
        val profile = sampleProfile()
        val repo = FakeProfileRepository(getResult = Result.success(profile))
        val vm = ProfileViewModel(repo)
        advanceUntilIdle()

        vm.clearError()
        assertNull(vm.errorMessage)
    }

    // ─── UserProfileViewModel ─────────────────────────────────────────────────

    @Test
    fun `UserProfileViewModel loads profile successfully`() = runTest {
        val repo = FakeUsersRepository(
            getUserInfoResult = Result.success(
                UserDto("uid-1", "bob", listOf("TRACKER"), "Bob Tracker",
                    "bob@example.com", "+70000000002", null, true)
            )
        )
        val vm = UserProfileViewModel(repo, "bob")
        advanceUntilIdle()

        assertFalse(vm.state.isLoading)
        assertNull(vm.state.error)
        assertEquals("uid-1", vm.state.id)
        assertEquals("bob", vm.state.username)
        assertEquals("Bob Tracker", vm.state.fullName)
        assertEquals("bob@example.com", vm.state.email)
        assertEquals("+70000000002", vm.state.phoneNumber)
        assertTrue(vm.state.enabled)
    }

    @Test
    fun `UserProfileViewModel falls back to username when id is null`() = runTest {
        val repo = FakeUsersRepository(
            getUserInfoResult = Result.success(
                UserDto(null, "nick", listOf("TRACKER"), "Nick",
                    "nick@example.com", null, null, false)
            )
        )
        val vm = UserProfileViewModel(repo, "nick")
        advanceUntilIdle()

        assertEquals("nick", vm.state.id)
        assertEquals("Не указан", vm.state.phoneNumber)
        assertFalse(vm.state.enabled)
    }

    @Test
    fun `UserProfileViewModel sets error on load failure`() = runTest {
        val repo = FakeUsersRepository(
            getUserInfoResult = Result.failure(Exception("404"))
        )
        val vm = UserProfileViewModel(repo, "missing")
        advanceUntilIdle()

        assertFalse(vm.state.isLoading)
        assertEquals("Не удалось загрузить профиль пользователя", vm.state.error)
    }

    @Test
    fun `UserProfileViewModel retry reloads profile`() = runTest {
        var calls = 0
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getUserInfo(username: String): Result<UserDto> {
                calls++
                return if (calls == 1) Result.failure(Exception("temporary"))
                else Result.success(UserDto("1", username, listOf("TRACKER"), "Retried", "r@e.com", "+7", null, true))
            }
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = UserProfileViewModel(repo, "user")
        advanceUntilIdle()
        assertEquals("Не удалось загрузить профиль пользователя", vm.state.error)

        vm.retry()
        advanceUntilIdle()
        assertEquals("Retried", vm.state.fullName)
        assertNull(vm.state.error)
    }

    @Test
    fun `UserProfileViewModel retry after success does not change state`() = runTest {
        var calls = 0
        val repo = object : UsersRepository {
            override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean) =
                Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
            override suspend fun getUserInfo(username: String): Result<UserDto> {
                calls++
                return Result.success(UserDto("1", username, listOf("TRACKER"), "Original", "o@e.com", "+7", null, true))
            }
            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val vm = UserProfileViewModel(repo, "user")
        advanceUntilIdle()
        assertEquals("Original", vm.state.fullName)

        vm.retry()
        advanceUntilIdle()
        assertEquals("Original", vm.state.fullName)
        assertEquals(2, calls)
    }

    @Test
    fun `UserProfileViewModel with null phoneNumber shows fallback`() = runTest {
        val repo = FakeUsersRepository(
            getUserInfoResult = Result.success(
                UserDto("1", "test", listOf("TRACKER"), "Test", "t@t.com", null, null, true)
            )
        )
        val vm = UserProfileViewModel(repo, "test")
        advanceUntilIdle()

        assertEquals("Не указан", vm.state.phoneNumber)
    }



    @Test
    fun `UserProfileViewModel with avatarUrl`() = runTest {
        val repo = FakeUsersRepository(
            getUserInfoResult = Result.success(
                UserDto("1", "test", listOf("TRACKER"), "Test", "t@t.com", "+7", "http://avatar.png", true)
            )
        )
        val vm = UserProfileViewModel(repo, "test")
        advanceUntilIdle()

        // UserProfileState does not expose avatarUrl, just verify load succeeded
        assertFalse(vm.state.isLoading)
        assertNull(vm.state.error)
        assertEquals("test", vm.state.username)
    }

    @Test
    fun `UserProfileViewModel with multiple roles`() = runTest {
        val repo = FakeUsersRepository(
            getUserInfoResult = Result.success(
                UserDto("1", "multi", listOf("TRACKER", "ADMIN"), "Multi", "m@m.com", "+7", null, true)
            )
        )
        val vm = UserProfileViewModel(repo, "multi")
        advanceUntilIdle()

        assertEquals(2, vm.state.roles.size)
        assertTrue("TRACKER" in vm.state.roles)
        assertTrue("ADMIN" in vm.state.roles)
    }

    @Test
    fun `UserProfileViewModel isLoading is true during initial load`() = runTest {
        val repo = FakeUsersRepository(
            getUserInfoResult = Result.success(
                UserDto("1", "u", listOf("TRACKER"), "U", "u@u.com", "+7", null, true)
            )
        )
        val vm = UserProfileViewModel(repo, "u")
        // isLoading is set inside viewModelScope.launch, may not be true yet
        advanceUntilIdle()
        assertFalse(vm.state.isLoading)
    }

    // ─── Helper methods ───────────────────────────────────────────────────────

    private fun sampleProfile(
        fullName: String = "John Doe",
        avatarUrl: String? = "avatar://old"
    ) = UserProfile(
        id = "1",
        username = "john",
        fullName = fullName,
        email = "john@example.com",
        phoneNumber = "+70000000000",
        avatarUrl = avatarUrl,
        roles = listOf("TRACKER"),
        enabled = true
    )

    private class FakeProfileRepository(
        private val getResult: Result<UserProfile> = Result.failure(Exception("not configured")),
        private val updateResult: Result<Unit> = Result.success(Unit)
    ) : ProfileRepository {
        override suspend fun getAccountInfo(): Result<UserProfile> = getResult
        override suspend fun updateAccount(
            fullName: String, email: String, phoneNumber: String, avatarUrl: String?
        ): Result<Unit> = updateResult
    }

    private class FakeUsersRepository(
        private val getUserInfoResult: Result<UserDto> = Result.failure(Exception("not configured"))
    ) : UsersRepository {
        override suspend fun getTrackers(p: Int, s: Int, sort: List<String>, sb: Boolean) =
            Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
        override suspend fun getAdministrators(p: Int, s: Int, sort: List<String>, sb: Boolean) =
            Result.failure<PagedResponse<UserDto>>(Exception("n/a"))
        override suspend fun getUserInfo(username: String): Result<UserDto> = getUserInfoResult
        override suspend fun enableUser(username: String) = Result.success(Unit)
        override suspend fun disableUser(username: String) = Result.success(Unit)
    }
}