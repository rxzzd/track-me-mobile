package com.example.track_me_mobile

import com.example.track_me_mobile.features.profile.domain.ProfileRepository
import com.example.track_me_mobile.features.profile.domain.models.UserProfile
import com.example.track_me_mobile.features.profile.presentation.ProfileViewModel
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
class ProfileViewModelTest {

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile success updates profile state`() = runTest {
        val expectedProfile = UserProfile(
            id = "1",
            username = "john_doe",
            fullName = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "+70000000000",
            avatarUrl = "avatar://base64",
            roles = listOf("Пользователь"),
            enabled = true
        )

        val repository = FakeProfileRepository(getResult = Result.success(expectedProfile))
        val viewModel = ProfileViewModel(repository)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
        assertEquals(expectedProfile, viewModel.profile)
    }

    @Test
    fun `loadProfile failure sets error message`() = runTest {
        val repository = FakeProfileRepository(getResult = Result.failure(Exception("network error")))
        val viewModel = ProfileViewModel(repository)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertEquals("Не удалось загрузить профиль", viewModel.errorMessage)
        assertNull(viewModel.profile)
    }

    @Test
    fun `saveProfile success updates profile and saveSuccess`() = runTest {
        val initialProfile = UserProfile(
            id = "1",
            username = "john_doe",
            fullName = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "+70000000000",
            avatarUrl = "avatar://old",
            roles = listOf("Пользователь"),
            enabled = true
        )

        val repository = FakeProfileRepository(
            getResult = Result.success(initialProfile),
            updateResult = Result.success(Unit)
        )
        val viewModel = ProfileViewModel(repository)

        advanceUntilIdle()

        viewModel.saveProfile(
            fullName = "Jane Doe",
            email = "jane.doe@example.com",
            phoneNumber = "+79999999999",
            newAvatarUrl = "avatar://new",
            onSuccess = {}
        )

        advanceUntilIdle()

        assertFalse(viewModel.isSaving)
        assertTrue(viewModel.saveSuccess)
        assertNull(viewModel.errorMessage)
        assertEquals("Jane Doe", viewModel.profile?.fullName)
        assertEquals("jane.doe@example.com", viewModel.profile?.email)
        assertEquals("+79999999999", viewModel.profile?.phoneNumber)
        assertEquals("avatar://new", viewModel.profile?.avatarUrl)
        assertEquals("avatar://new", repository.lastUpdateParams?.avatarUrl)
    }

    @Test
    fun `saveProfile failure sets error message and keeps profile unchanged`() = runTest {
        val initialProfile = UserProfile(
            id = "1",
            username = "john_doe",
            fullName = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "+70000000000",
            avatarUrl = "avatar://old",
            roles = listOf("Пользователь"),
            enabled = true
        )

        val repository = FakeProfileRepository(
            getResult = Result.success(initialProfile),
            updateResult = Result.failure(Exception("save failed"))
        )
        val viewModel = ProfileViewModel(repository)

        advanceUntilIdle()

        viewModel.saveProfile(
            fullName = "Jane Doe",
            email = "jane.doe@example.com",
            phoneNumber = "+79999999999",
            newAvatarUrl = "avatar://new",
            onSuccess = {}
        )

        advanceUntilIdle()

        assertFalse(viewModel.isSaving)
        assertFalse(viewModel.saveSuccess)
        assertEquals("Не удалось сохранить изменения", viewModel.errorMessage)
        assertEquals("John Doe", viewModel.profile?.fullName)
        assertEquals("john.doe@example.com", viewModel.profile?.email)
        assertEquals("+70000000000", viewModel.profile?.phoneNumber)
        assertEquals("avatar://old", viewModel.profile?.avatarUrl)
    }

    private class FakeProfileRepository(
        var getResult: Result<UserProfile> = Result.failure(Exception("not configured")),
        var updateResult: Result<Unit> = Result.success(Unit)
    ) : ProfileRepository {

        var lastUpdateParams: UpdateParams? = null

        override suspend fun getAccountInfo(): Result<UserProfile> = getResult

        override suspend fun updateAccount(
            fullName: String,
            email: String,
            phoneNumber: String,
            avatarUrl: String?
        ): Result<Unit> {
            lastUpdateParams = UpdateParams(fullName, email, phoneNumber, avatarUrl)
            return updateResult
        }

        data class UpdateParams(
            val fullName: String,
            val email: String,
            val phoneNumber: String,
            val avatarUrl: String?
        )
    }
}