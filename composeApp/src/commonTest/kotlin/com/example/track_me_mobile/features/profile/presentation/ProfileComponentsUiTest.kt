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
class ProfileComponentsUiTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== ProfileViewModel Tests ====================

    @Test
    fun `ProfileViewModel loads profile successfully`() = runTest {
        val profile = UserProfile(
            id = "1", username = "ivanov", fullName = "Иван Иванов",
            email = "ivan@example.com", phoneNumber = "+7 999 123 45 67",
            avatarUrl = null, roles = listOf("ADMIN"), enabled = true
        )
        val viewModel = ProfileViewModel(FakeProfileRepository(profile = profile))
        advanceUntilIdle()

        assertNotNull(viewModel.profile)
        assertEquals("Иван Иванов", viewModel.profile?.fullName)
        assertEquals("ivan@example.com", viewModel.profile?.email)
        assertEquals("+7 999 123 45 67", viewModel.profile?.phoneNumber)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `ProfileViewModel load failure sets error message`() = runTest {
        val viewModel = ProfileViewModel(FakeProfileRepository(loadResult = Result.failure(Exception("fail"))))
        advanceUntilIdle()

        assertNull(viewModel.profile)
        assertEquals("Не удалось загрузить профиль", viewModel.errorMessage)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `ProfileViewModel saveProfile updates profile on success`() = runTest {
        val profile = UserProfile(
            id = "1", username = "ivanov", fullName = "Иван Иванов",
            email = "ivan@example.com", phoneNumber = "+7 999 123 45 67",
            avatarUrl = null, roles = listOf("ADMIN"), enabled = true
        )
        val viewModel = ProfileViewModel(FakeProfileRepository(profile = profile))
        advanceUntilIdle()

        var saved = false
        viewModel.saveProfile(
            fullName = "Новое Имя",
            email = "new@example.com",
            phoneNumber = "+7 111 222 33 44",
            onSuccess = { saved = true }
        )
        advanceUntilIdle()

        assertTrue(saved)
        assertTrue(viewModel.saveSuccess)
        assertEquals("Новое Имя", viewModel.profile?.fullName)
        assertEquals("new@example.com", viewModel.profile?.email)
        assertEquals("+7 111 222 33 44", viewModel.profile?.phoneNumber)
    }

    @Test
    fun `ProfileViewModel saveProfile with new avatar updates avatarUrl`() = runTest {
        val profile = UserProfile(
            id = "1", username = "ivanov", fullName = "Иван",
            email = "i@e.com", phoneNumber = null,
            avatarUrl = "old-avatar", roles = listOf("ADMIN"), enabled = true
        )
        val viewModel = ProfileViewModel(FakeProfileRepository(profile = profile))
        advanceUntilIdle()

        viewModel.saveProfile(
            fullName = "Иван", email = "i@e.com", phoneNumber = "",
            newAvatarUrl = "new-avatar-data",
            onSuccess = {}
        )
        advanceUntilIdle()

        assertEquals("new-avatar-data", viewModel.profile?.avatarUrl)
    }

    @Test
    fun `ProfileViewModel saveProfile preserves old avatar when no new avatar`() = runTest {
        val profile = UserProfile(
            id = "1", username = "ivanov", fullName = "Иван",
            email = "i@e.com", phoneNumber = null,
            avatarUrl = "old-avatar", roles = listOf("ADMIN"), enabled = true
        )
        val viewModel = ProfileViewModel(FakeProfileRepository(profile = profile))
        advanceUntilIdle()

        viewModel.saveProfile(
            fullName = "Иван", email = "i@e.com", phoneNumber = "",
            onSuccess = {}
        )
        advanceUntilIdle()

        assertEquals("old-avatar", viewModel.profile?.avatarUrl)
    }

    @Test
    fun `ProfileViewModel saveProfile failure sets error message`() = runTest {
        val profile = UserProfile(
            id = "1", username = "ivanov", fullName = "Иван",
            email = "i@e.com", phoneNumber = null,
            avatarUrl = null, roles = listOf("ADMIN"), enabled = true
        )
        val viewModel = ProfileViewModel(FakeProfileRepository(
            profile = profile,
            saveResult = Result.failure(Exception("save fail"))
        ))
        advanceUntilIdle()

        viewModel.saveProfile(fullName = "Иван", email = "i@e.com", phoneNumber = "", onSuccess = {})
        advanceUntilIdle()

        assertEquals("Не удалось сохранить изменения", viewModel.errorMessage)
        assertFalse(viewModel.isSaving)
    }

    @Test
    fun `ProfileViewModel clearError resets error message`() = runTest {
        val viewModel = ProfileViewModel(FakeProfileRepository(loadResult = Result.failure(Exception("fail"))))
        advanceUntilIdle()

        assertNotNull(viewModel.errorMessage)
        viewModel.clearError()
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `ProfileViewModel isLoading is true during load`() = runTest {
        val viewModel = ProfileViewModel(FakeProfileRepository(profile = UserProfile(
            id = "1", username = "u", fullName = "N", email = "e",
            phoneNumber = null, avatarUrl = null, roles = emptyList(), enabled = true
        )))
        assertTrue(viewModel.isLoading)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading)
    }

    // ==================== UserProfileViewModel Tests ====================

    @Test
    fun `UserProfileViewModel loads user profile successfully`() = runTest {
        val userDto = UserDto(
            id = "1", username = "petrov", roles = listOf("TRACKER"),
            fullName = "Петр Петров", email = "petr@example.com",
            phoneNumber = "+7 111 222 33 44", avatarUrl = null, enabled = true
        )
        val viewModel = UserProfileViewModel(FakeUsersRepository(userInfoResult = Result.success(userDto)), "petrov")
        advanceUntilIdle()

        assertFalse(viewModel.state.isLoading)
        assertNull(viewModel.state.error)
        assertEquals("Петр Петров", viewModel.state.fullName)
        assertEquals("petr@example.com", viewModel.state.email)
        assertEquals("+7 111 222 33 44", viewModel.state.phoneNumber)
        assertTrue(viewModel.state.enabled)
    }

    @Test
    fun `UserProfileViewModel load failure sets error`() = runTest {
        val viewModel = UserProfileViewModel(
            FakeUsersRepository(userInfoResult = Result.failure(Exception("fail"))),
            "unknown"
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.isLoading)
        assertEquals("Не удалось загрузить профиль пользователя", viewModel.state.error)
    }

    @Test
    fun `UserProfileViewModel retry reloads profile`() = runTest {
        var callCount = 0
        val repository = object : UsersRepository {
            override suspend fun getTrackers(page: Int, size: Int, sort: List<String>, showBlocked: Boolean): Result<PagedResponse<UserDto>> =
                Result.success(PagedResponse(emptyList(), com.example.track_me_mobile.features.users.data.model.PageInfo(0, 0, 0, 0)))
            override suspend fun getAdministrators(page: Int, size: Int, sort: List<String>, showBlocked: Boolean): Result<PagedResponse<UserDto>> =
                Result.success(PagedResponse(emptyList(), com.example.track_me_mobile.features.users.data.model.PageInfo(0, 0, 0, 0)))
            override suspend fun getUserInfo(username: String): Result<UserDto> {
                callCount++
                return if (callCount == 1) Result.failure(Exception("fail"))
                else Result.success(UserDto("1", "u", listOf("TRACKER"), "Name", "e", null, null, true))
            }
            override suspend fun enableUser(username: String): Result<Unit> = Result.success(Unit)
            override suspend fun disableUser(username: String): Result<Unit> = Result.success(Unit)
        }
        val viewModel = UserProfileViewModel(repository, "u")
        advanceUntilIdle()

        assertNotNull(viewModel.state.error)

        viewModel.retry()
        advanceUntilIdle()

        assertEquals("Name", viewModel.state.fullName)
        assertNull(viewModel.state.error)
        assertEquals(2, callCount)
    }

    @Test
    fun `UserProfileViewModel uses username as fallback when id is null`() = runTest {
        val userDto = UserDto(
            id = null, username = "noiduser", roles = listOf("TRACKER"),
            fullName = "No ID User", email = "n@e.com",
            phoneNumber = null, avatarUrl = null, enabled = false
        )
        val viewModel = UserProfileViewModel(FakeUsersRepository(userInfoResult = Result.success(userDto)), "noiduser")
        advanceUntilIdle()

        assertEquals("noiduser", viewModel.state.id)
        assertEquals("Не указан", viewModel.state.phoneNumber)
        assertFalse(viewModel.state.enabled)
    }

    @Test
    fun `UserProfileViewModel shows loading initially`() = runTest {
        val viewModel = UserProfileViewModel(
            FakeUsersRepository(userInfoResult = Result.success(UserDto(
                "1", "u", listOf(), "N", "e", null, null, true
            ))),
            "u"
        )
        assertTrue(viewModel.state.isLoading)
        advanceUntilIdle()
        assertFalse(viewModel.state.isLoading)
    }

    // ==================== Fake Repositories ====================

    private class FakeProfileRepository(
        private val profile: UserProfile? = null,
        private val loadResult: Result<UserProfile>? = null,
        private val saveResult: Result<Unit> = Result.success(Unit)
    ) : ProfileRepository {
        override suspend fun getAccountInfo(): Result<UserProfile> =
            loadResult ?: if (profile != null) Result.success(profile) else Result.failure(Exception("no profile"))

        override suspend fun updateAccount(
            fullName: String, email: String, phoneNumber: String, avatarUrl: String?
        ): Result<Unit> = saveResult
    }

    private class FakeUsersRepository(
        private val userInfoResult: Result<UserDto> = Result.failure(Exception("no user"))
    ) : UsersRepository {
        override suspend fun getTrackers(page: Int, size: Int, sort: List<String>, showBlocked: Boolean): Result<PagedResponse<UserDto>> =
            Result.success(PagedResponse(emptyList(), com.example.track_me_mobile.features.users.data.model.PageInfo(0, 0, 0, 0)))
        override suspend fun getAdministrators(page: Int, size: Int, sort: List<String>, showBlocked: Boolean): Result<PagedResponse<UserDto>> =
            Result.success(PagedResponse(emptyList(), com.example.track_me_mobile.features.users.data.model.PageInfo(0, 0, 0, 0)))
        override suspend fun getUserInfo(username: String): Result<UserDto> = userInfoResult
        override suspend fun enableUser(username: String): Result<Unit> = Result.success(Unit)
        override suspend fun disableUser(username: String): Result<Unit> = Result.success(Unit)
    }
}