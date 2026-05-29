package com.example.track_me_mobile.features.auth.presentation

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import com.example.track_me_mobile.features.auth.domain.AuthSessionSaver
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

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

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
    fun `loginFromWebView success saves user and navigates`() = runTest {
        val expectedUserInfo = UserInfo(
            id = "user1",
            username = "john_doe",
            fullName = "John Doe",
            email = "john.doe@example.com",
            roles = listOf(Role.TRACKER)
        )

        val repository = FakeAuthRepository(Result.success(expectedUserInfo))
        val sessionSaver = FakeSessionSaver()
        val userInfoHolder = UserInfoHolder()
        var navigatedRole: Role? = null

        val viewModel = LoginViewModel(repository, sessionSaver, userInfoHolder)

        viewModel.loginFromWebView("SESSION=abc123", onNavigate = { role -> navigatedRole = role })

        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
        assertEquals(Role.TRACKER, navigatedRole)
        assertEquals(expectedUserInfo, userInfoHolder.userInfo)
        assertEquals("abc123", sessionSaver.savedSessionValue)
    }

    @Test
    fun `loginFromWebView failure sets error message`() = runTest {
        val repository = FakeAuthRepository(Result.failure(Exception("user fetch failure")))
        val sessionSaver = FakeSessionSaver()
        val userInfoHolder = UserInfoHolder()
        var navigatedRole: Role? = null

        val viewModel = LoginViewModel(repository, sessionSaver, userInfoHolder)

        viewModel.loginFromWebView("SESSION=abc123", onNavigate = { role -> navigatedRole = role })

        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertEquals("Не удалось загрузить профиль", viewModel.errorMessage)
        assertNull(navigatedRole)
        assertNull(userInfoHolder.userInfo)
    }

    @Test
    fun `loginFromWebView exception shows generic error`() = runTest {
        val repository = FakeAuthRepository(Result.failure(Exception("should not be used")))
        val sessionSaver = FailingSessionSaver()
        val userInfoHolder = UserInfoHolder()
        var navigatedRole: Role? = null

        val viewModel = LoginViewModel(repository, sessionSaver, userInfoHolder)

        viewModel.loginFromWebView("SESSION=abc123", onNavigate = { role -> navigatedRole = role })

        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertEquals("Произошла ошибка. Попробуйте ещё раз.", viewModel.errorMessage)
        assertNull(navigatedRole)
        assertNull(userInfoHolder.userInfo)
    }

    private class FakeAuthRepository(
        private val result: Result<UserInfo>
    ) : AuthRepository {
        override suspend fun getUserInfo(): Result<UserInfo> = result
    }

    private class FakeSessionSaver : AuthSessionSaver {
        var savedSessionValue: String? = null

        override suspend fun saveSession(cookieString: String) {
            savedSessionValue = cookieString
                .split(";")
                .map { it.trim() }
                .find { it.startsWith("SESSION=") }
                ?.substringAfter("SESSION=")
                ?.trim()
        }
    }

    private class FailingSessionSaver : AuthSessionSaver {
        override suspend fun saveSession(cookieString: String) {
            throw Exception("save failed")
        }
    }
}
