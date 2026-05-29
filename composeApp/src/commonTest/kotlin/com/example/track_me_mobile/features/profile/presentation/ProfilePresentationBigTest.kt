package com.example.track_me_mobile.features.profile.presentation

import com.example.track_me_mobile.features.profile.domain.ProfileRepository
import com.example.track_me_mobile.features.profile.domain.models.UserProfile
import com.example.track_me_mobile.features.users.data.model.UserDto
import com.example.track_me_mobile.features.users.domain.UsersRepository
import com.example.track_me_mobile.features.users.data.model.PagedResponse
import com.example.track_me_mobile.features.users.data.model.PageInfo
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
class ProfilePresentationBigTest {

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
    fun `ProfileViewModel clearError resets error message`() = runTest {
        val repository = FakeProfileRepository(
            getResult = Result.failure(Exception("fail"))
        )
        val viewModel = ProfileViewModel(repository)
        advanceUntilIdle()

        assertEquals("Не удалось загрузить профиль", viewModel.errorMessage)
        viewModel.clearError()
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `ProfileViewModel saveProfile uses existing avatar when newAvatarUrl is null`() = runTest {
        val profile = sampleProfile(avatarUrl = "existing-avatar")
        val repository = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.success(Unit)
        )
        val viewModel = ProfileViewModel(repository)
        advanceUntilIdle()

        viewModel.saveProfile("New Name", "new@mail.com", "+7111", newAvatarUrl = null) {}
        advanceUntilIdle()

        assertEquals("existing-avatar", viewModel.profile?.avatarUrl)
        assertTrue(viewModel.saveSuccess)
    }

    @Test
    fun `ProfileViewModel saveProfile uses empty avatar when profile and new avatar are null`() = runTest {
        val profile = sampleProfile(avatarUrl = null)
        val repository = FakeProfileRepository(
            getResult = Result.success(profile),
            updateResult = Result.success(Unit)
        )
        val viewModel = ProfileViewModel(repository)
        advanceUntilIdle()

        viewModel.saveProfile("Name", "mail@test.com", "+7", newAvatarUrl = null) {}
        advanceUntilIdle()

        assertNull(viewModel.profile?.avatarUrl)
    }

    @Test
    fun `ProfileViewModel reload profile via loadProfile after initial load`() = runTest {
        val first = sampleProfile(fullName = "First")
        val second = sampleProfile(fullName = "Second")
        var call = 0
        val repository = object : ProfileRepository {
            override suspend fun getAccountInfo(): Result<UserProfile> {
                call++
                return Result.success(if (call == 1) first else second)
            }
            override suspend fun updateAccount(
                fullName: String,
                email: String,
                phoneNumber: String,
                avatarUrl: String?
            ): Result<Unit> = Result.success(Unit)
        }
        val viewModel = ProfileViewModel(repository)
        advanceUntilIdle()
        assertEquals("First", viewModel.profile?.fullName)

        viewModel.loadProfile()
        advanceUntilIdle()
        assertEquals("Second", viewModel.profile?.fullName)
    }

    @Test
    fun `UserProfileViewModel loads user profile successfully`() = runTest {
        val repository = FakeUsersRepository(
            getUserInfoResult = Result.success(
                UserDto(
                    id = "uid-1",
                    username = "bob",
                    roles = listOf("TRACKER"),
                    fullName = "Bob Tracker",
                    email = "bob@example.com",
                    phoneNumber = "+70000000002",
                    avatarUrl = null,
                    enabled = true
                )
            )
        )
        val viewModel = UserProfileViewModel(repository, "bob")
        advanceUntilIdle()

        assertFalse(viewModel.state.isLoading)
        assertNull(viewModel.state.error)
        assertEquals("uid-1", viewModel.state.id)
        assertEquals("bob", viewModel.state.username)
        assertEquals("Bob Tracker", viewModel.state.fullName)
        assertEquals("bob@example.com", viewModel.state.email)
        assertEquals("+70000000002", viewModel.state.phoneNumber)
        assertTrue(viewModel.state.enabled)
    }

    @Test
    fun `UserProfileViewModel falls back to username when id is null`() = runTest {
        val repository = FakeUsersRepository(
            getUserInfoResult = Result.success(
                UserDto(
                    id = null,
                    username = "nick",
                    roles = listOf("TRACKER"),
                    fullName = "Nick",
                    email = "nick@example.com",
                    phoneNumber = null,
                    avatarUrl = null,
                    enabled = false
                )
            )
        )
        val viewModel = UserProfileViewModel(repository, "nick")
        advanceUntilIdle()

        assertEquals("nick", viewModel.state.id)
        assertEquals("Не указан", viewModel.state.phoneNumber)
        assertFalse(viewModel.state.enabled)
    }

    @Test
    fun `UserProfileViewModel sets error on load failure`() = runTest {
        val repository = FakeUsersRepository(
            getUserInfoResult = Result.failure(Exception("404"))
        )
        val viewModel = UserProfileViewModel(repository, "missing")
        advanceUntilIdle()

        assertFalse(viewModel.state.isLoading)
        assertEquals("Не удалось загрузить профиль пользователя", viewModel.state.error)
    }

    @Test
    fun `UserProfileViewModel retry reloads profile`() = runTest {
        var calls = 0
        val repository = object : UsersRepository {
            override suspend fun getTrackers(
                page: Int,
                size: Int,
                sort: List<String>,
                showBlocked: Boolean
            ) = Result.failure<PagedResponse<UserDto>>(Exception("n/a"))

            override suspend fun getAdministrators(
                page: Int,
                size: Int,
                sort: List<String>,
                showBlocked: Boolean
            ) = Result.failure<PagedResponse<UserDto>>(Exception("n/a"))

            override suspend fun getUserInfo(username: String): Result<UserDto> {
                calls++
                return if (calls == 1) {
                    Result.failure(Exception("temporary"))
                } else {
                    Result.success(
                        UserDto(
                            id = "1",
                            username = username,
                            roles = listOf("TRACKER"),
                            fullName = "Retried",
                            email = "r@e.com",
                            phoneNumber = "+7",
                            avatarUrl = null,
                            enabled = true
                        )
                    )
                }
            }

            override suspend fun enableUser(username: String) = Result.success(Unit)
            override suspend fun disableUser(username: String) = Result.success(Unit)
        }
        val viewModel = UserProfileViewModel(repository, "user")
        advanceUntilIdle()
        assertEquals("Не удалось загрузить профиль пользователя", viewModel.state.error)

        viewModel.retry()
        advanceUntilIdle()
        assertEquals("Retried", viewModel.state.fullName)
        assertNull(viewModel.state.error)
    }

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
            fullName: String,
            email: String,
            phoneNumber: String,
            avatarUrl: String?
        ): Result<Unit> = updateResult
    }

    private class FakeUsersRepository(
        private val getUserInfoResult: Result<UserDto> = Result.failure(Exception("not configured"))
    ) : UsersRepository {
        override suspend fun getTrackers(
            page: Int,
            size: Int,
            sort: List<String>,
            showBlocked: Boolean
        ) = Result.failure<PagedResponse<UserDto>>(Exception("n/a"))

        override suspend fun getAdministrators(
            page: Int,
            size: Int,
            sort: List<String>,
            showBlocked: Boolean
        ) = Result.failure<PagedResponse<UserDto>>(Exception("n/a"))

        override suspend fun getUserInfo(username: String): Result<UserDto> = getUserInfoResult
        override suspend fun enableUser(username: String) = Result.success(Unit)
        override suspend fun disableUser(username: String) = Result.success(Unit)
    }
}
