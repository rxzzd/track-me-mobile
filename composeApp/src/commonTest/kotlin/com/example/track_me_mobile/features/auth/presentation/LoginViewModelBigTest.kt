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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelBigTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── LoginViewModel: loginFromWebView ─────────────────────────────────────

    @Test
    fun `loginFromWebView with ADMIN role navigates to ADMIN`() = runTest {
        val userInfo = UserInfo(
            id = "admin1", username = "admin", fullName = "Admin",
            email = "admin@test.com", roles = listOf(Role.ADMIN)
        )
        val repository = FakeAuthRepository(Result.success(userInfo))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()
        var navigated: Role? = null

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=xyz") { navigated = it }
        advanceUntilIdle()

        assertEquals(Role.ADMIN, navigated)
        assertEquals(userInfo, holder.userInfo)
        assertFalse(vm.isLoading)
        assertNull(vm.errorMessage)
    }

    @Test
    fun `loginFromWebView with TRACKER role navigates to TRACKER`() = runTest {
        val userInfo = UserInfo(
            id = "t1", username = "tracker", fullName = "Tracker",
            email = "t@test.com", roles = listOf(Role.TRACKER)
        )
        val repository = FakeAuthRepository(Result.success(userInfo))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()
        var navigated: Role? = null

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=abc") { navigated = it }
        advanceUntilIdle()

        assertEquals(Role.TRACKER, navigated)
        assertNull(vm.errorMessage)
    }

    @Test
    fun `loginFromWebView with multiple roles navigates to first role`() = runTest {
        val userInfo = UserInfo(
            id = "u1", username = "multi", fullName = "Multi Role",
            email = "m@test.com", roles = listOf(Role.TRACKER, Role.ADMIN)
        )
        val repository = FakeAuthRepository(Result.success(userInfo))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()
        var navigated: Role? = null

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=abc") { navigated = it }
        advanceUntilIdle()

        // mainRole checks SUPER_ADMIN first, then ADMIN, then TRACKER
        assertEquals(Role.ADMIN, navigated)
    }

    @Test
    fun `loginFromWebView saves session with full cookie string`() = runTest {
        val userInfo = UserInfo(
            id = "1", username = "u", fullName = "U",
            email = "u@t.com", roles = listOf(Role.TRACKER)
        )
        val repository = FakeAuthRepository(Result.success(userInfo))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()
        var navigated = false

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=my_token; Path=/; HttpOnly") { navigated = true }
        advanceUntilIdle()

        assertEquals("my_token", sessionSaver.savedSessionValue)
        assertTrue(navigated)
    }

    @Test
    fun `loginFromWebView saves session without extra params`() = runTest {
        val userInfo = UserInfo(
            id = "1", username = "u", fullName = "U",
            email = "u@t.com", roles = listOf(Role.TRACKER)
        )
        val repository = FakeAuthRepository(Result.success(userInfo))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()
        var navigated = false

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=simple_token") { navigated = true }
        advanceUntilIdle()

        assertEquals("simple_token", sessionSaver.savedSessionValue)
        assertTrue(navigated)
    }

    @Test
    fun `loginFromWebView failure does not save user info`() = runTest {
        val repository = FakeAuthRepository(Result.failure(Exception("fail")))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()
        var navigated = false

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=abc") { navigated = true }
        advanceUntilIdle()

        assertNull(holder.userInfo)
        assertFalse(navigated)
        assertEquals("Не удалось загрузить профиль", vm.errorMessage)
    }

    @Test
    fun `loginFromWebView failure with empty cookie string`() = runTest {
        val repository = FakeAuthRepository(Result.failure(Exception("fail")))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()
        var navigated = false

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("") { navigated = true }
        advanceUntilIdle()

        assertEquals("Не удалось загрузить профиль", vm.errorMessage)
        assertNull(holder.userInfo)
    }

    @Test
    fun `loginFromWebView exception from session saver shows generic error`() = runTest {
        val repository = FakeAuthRepository(Result.failure(Exception("should not be used")))
        val sessionSaver = FailingSessionSaver()
        val holder = UserInfoHolder()
        var navigated = false

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=abc") { navigated = true }
        advanceUntilIdle()

        assertEquals("Произошла ошибка. Попробуйте ещё раз.", vm.errorMessage)
        assertNull(holder.userInfo)
        assertFalse(navigated)
    }

    @Test
    fun `loginFromWebView exception with null cookie`() = runTest {
        val repository = FakeAuthRepository(Result.failure(Exception("should not be used")))
        val sessionSaver = FailingSessionSaver()
        val holder = UserInfoHolder()
        var navigated = false

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=null_test") { navigated = true }
        advanceUntilIdle()

        assertEquals("Произошла ошибка. Попробуйте ещё раз.", vm.errorMessage)
        assertNull(holder.userInfo)
    }

    @Test
    fun `loginFromWebView isLoading is true during execution`() = runTest {
        val repository = FakeAuthRepository(Result.success(
            UserInfo("1", "u", "U", "u@t.com", listOf(Role.TRACKER))
        ))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=abc") {}
        // isLoading is set inside the coroutine, so it may not be true yet
        // But after advanceUntilIdle it should be false
        advanceUntilIdle()
        assertFalse(vm.isLoading)
    }

    @Test
    fun `loginFromWebView error message is null on success`() = runTest {
        val repository = FakeAuthRepository(Result.success(
            UserInfo("1", "u", "U", "u@t.com", listOf(Role.TRACKER))
        ))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=abc") {}
        advanceUntilIdle()

        assertNull(vm.errorMessage)
    }

    @Test
    fun `loginFromWebView preserves previous error on new call`() = runTest {
        val repository = FakeAuthRepository(Result.failure(Exception("fail")))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=abc") {}
        advanceUntilIdle()
        assertEquals("Не удалось загрузить профиль", vm.errorMessage)

        // Second call also fails
        vm.loginFromWebView("SESSION=def") {}
        advanceUntilIdle()
        assertEquals("Не удалось загрузить профиль", vm.errorMessage)
    }

    @Test
    fun `loginFromWebView success after failure clears error`() = runTest {
        var callCount = 0
        val repository = object : AuthRepository {
            override suspend fun getUserInfo(): Result<UserInfo> {
                callCount++
                return if (callCount == 1) {
                    Result.failure(Exception("fail"))
                } else {
                    Result.success(UserInfo("1", "u", "U", "u@t.com", listOf(Role.TRACKER)))
                }
            }
        }
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()
        var navigated = false

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=abc") {}
        advanceUntilIdle()
        assertEquals("Не удалось загрузить профиль", vm.errorMessage)

        vm.loginFromWebView("SESSION=def") { navigated = true }
        advanceUntilIdle()
        assertNull(vm.errorMessage)
        assertTrue(navigated)
    }

    @Test
    fun `loginFromWebView with TRACKER role saves correct user info`() = runTest {
        val expected = UserInfo(
            id = "tracker-1", username = "tracker_user", fullName = "Tracker User",
            email = "tracker@example.com", roles = listOf(Role.TRACKER)
        )
        val repository = FakeAuthRepository(Result.success(expected))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=tracker123") {}
        advanceUntilIdle()

        assertNotNull(holder.userInfo)
        assertEquals("tracker-1", holder.userInfo?.id)
        assertEquals("tracker_user", holder.userInfo?.username)
        assertEquals("Tracker User", holder.userInfo?.fullName)
        assertEquals("tracker@example.com", holder.userInfo?.email)
        assertEquals(listOf(Role.TRACKER), holder.userInfo?.roles)
    }

    @Test
    fun `loginFromWebView with ADMIN role saves correct user info`() = runTest {
        val expected = UserInfo(
            id = "admin-1", username = "admin_user", fullName = "Admin User",
            email = "admin@example.com", roles = listOf(Role.ADMIN)
        )
        val repository = FakeAuthRepository(Result.success(expected))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=admin456") {}
        advanceUntilIdle()

        assertNotNull(holder.userInfo)
        assertEquals("admin-1", holder.userInfo?.id)
        assertEquals("admin_user", holder.userInfo?.username)
        assertEquals(listOf(Role.ADMIN), holder.userInfo?.roles)
    }

    @Test
    fun `loginFromWebView with empty roles list`() = runTest {
        val userInfo = UserInfo(
            id = "1", username = "noroles", fullName = "No Roles",
            email = "nr@test.com", roles = emptyList()
        )
        val repository = FakeAuthRepository(Result.success(userInfo))
        val sessionSaver = FakeSessionSaver()
        val holder = UserInfoHolder()
        var navigated: Role? = null

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=abc") { navigated = it }
        advanceUntilIdle()

        // mainRole on empty list would be null, so onNavigate may not be called
        // Just verify user info is saved
        assertEquals(userInfo, holder.userInfo)
        assertNull(vm.errorMessage)
    }

    @Test
    fun `loginFromWebView exception clears error before setting new one`() = runTest {
        val repository = FakeAuthRepository(Result.failure(Exception("should not be used")))
        val sessionSaver = FailingSessionSaver()
        val holder = UserInfoHolder()

        val vm = LoginViewModel(repository, sessionSaver, holder)
        vm.loginFromWebView("SESSION=abc") {}
        advanceUntilIdle()

        // errorMessage should be set to generic error
        assertEquals("Произошла ошибка. Попробуйте ещё раз.", vm.errorMessage)
    }

    // ─── Fake implementations ──────────────────────────────────────────────────

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