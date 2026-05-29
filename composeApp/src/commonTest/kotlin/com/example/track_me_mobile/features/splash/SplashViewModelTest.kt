package com.example.track_me_mobile.features.splash

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
import com.example.track_me_mobile.core.network.SessionProvider
import com.example.track_me_mobile.features.auth.domain.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

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
    fun `checkAuth with no session navigates to Login`() = runTest {
        val session = FakeSessionProvider(session = null)
        val viewModel = SplashViewModel(session, FakeAuthRepository(), UserInfoHolder())
        var destination: SplashDestination? = null

        viewModel.checkAuth { destination = it }
        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals(SplashDestination.Login, destination)
    }

    @Test
    fun `checkAuth with blank session navigates to Login`() = runTest {
        val viewModel = SplashViewModel(FakeSessionProvider("   "), FakeAuthRepository(), UserInfoHolder())
        var destination: SplashDestination? = null

        viewModel.checkAuth { destination = it }
        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals(SplashDestination.Login, destination)
    }

    @Test
    fun `checkAuth with valid admin session navigates to AdminHome`() = runTest {
        val userInfoHolder = UserInfoHolder()
        val viewModel = SplashViewModel(
            FakeSessionProvider("valid-session"),
            FakeAuthRepository(
                Result.success(UserInfo("1", "admin", "Admin", "a@a.com", listOf(Role.ADMIN)))
            ),
            userInfoHolder
        )
        var destination: SplashDestination? = null

        viewModel.checkAuth { destination = it }
        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals(SplashDestination.AdminHome, destination)
        assertEquals("admin", userInfoHolder.userInfo?.username)
    }

    @Test
    fun `checkAuth with valid super admin session navigates to AdminHome`() = runTest {
        val viewModel = SplashViewModel(
            FakeSessionProvider("session"),
            FakeAuthRepository(
                Result.success(UserInfo("1", "super", "Super", null, listOf(Role.SUPER_ADMIN)))
            ),
            UserInfoHolder()
        )
        var destination: SplashDestination? = null

        viewModel.checkAuth { destination = it }
        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals(SplashDestination.AdminHome, destination)
    }

    @Test
    fun `checkAuth with valid tracker session navigates to TrackerHome`() = runTest {
        val viewModel = SplashViewModel(
            FakeSessionProvider("session"),
            FakeAuthRepository(
                Result.success(UserInfo("1", "tracker", "Tracker", null, listOf(Role.TRACKER)))
            ),
            UserInfoHolder()
        )
        var destination: SplashDestination? = null

        viewModel.checkAuth { destination = it }
        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals(SplashDestination.TrackerHome, destination)
    }

    @Test
    fun `checkAuth with unknown role navigates to Login`() = runTest {
        val viewModel = SplashViewModel(
            FakeSessionProvider("session"),
            FakeAuthRepository(
                Result.success(UserInfo("1", "user", "User", null, listOf(Role.UNKNOWN)))
            ),
            UserInfoHolder()
        )
        var destination: SplashDestination? = null

        viewModel.checkAuth { destination = it }
        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals(SplashDestination.Login, destination)
    }

    @Test
    fun `checkAuth with invalid session clears storage and navigates to Login`() = runTest {
        val session = FakeSessionProvider("expired-session")
        val viewModel = SplashViewModel(
            session,
            FakeAuthRepository(Result.failure(Exception("401 Unauthorized"))),
            UserInfoHolder()
        )
        var destination: SplashDestination? = null

        viewModel.checkAuth { destination = it }
        advanceTimeBy(1100)
        advanceUntilIdle()

        assertEquals(SplashDestination.Login, destination)
        assertEquals("Сессия истекла", viewModel.errorMessage)
        assertTrue(session.cleared)
    }

    @Test
    fun `checkAuth handles unexpected exception`() = runTest {
        val viewModel = SplashViewModel(
            FakeSessionProvider("session", throwOnGet = true),
            FakeAuthRepository(),
            UserInfoHolder()
        )
        var destination: SplashDestination? = null

        viewModel.checkAuth { destination = it }
        advanceTimeBy(1100)
        advanceUntilIdle()

        assertEquals(SplashDestination.Login, destination)
        assertEquals("Ошибка проверки авторизации", viewModel.errorMessage)
    }

    @Test
    fun `SplashDestination enum contains all routes`() {
        assertEquals(3, SplashDestination.entries.size)
        assertTrue(SplashDestination.entries.contains(SplashDestination.Login))
        assertTrue(SplashDestination.entries.contains(SplashDestination.AdminHome))
        assertTrue(SplashDestination.entries.contains(SplashDestination.TrackerHome))
    }

    private class FakeSessionProvider(
        private val session: String? = null,
        private val throwOnGet: Boolean = false
    ) : SessionProvider {
        var cleared = false

        override suspend fun get(): String? {
            if (throwOnGet) throw RuntimeException("storage error")
            return session
        }

        override suspend fun clear() {
            cleared = true
        }
    }

    private class FakeAuthRepository(
        private val result: Result<UserInfo> = Result.success(
            UserInfo("1", "user", "User", null, listOf(Role.TRACKER))
        )
    ) : AuthRepository {
        override suspend fun getUserInfo(): Result<UserInfo> = result
    }
}
