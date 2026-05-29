package com.example.track_me_mobile.features.auth.presentation

import cafe.adriel.voyager.core.screen.Screen
import kotlin.test.Test
import kotlin.test.assertNotNull

class AuthScreenProviderTest {

    @Test
    fun `login screen can be instantiated`() {
        val screen = LoginScreen()
        assertNotNull(screen)
    }

    @Test
    fun `getLoginWebViewScreen returns screen`() {
        val screen: Screen = getLoginWebViewScreen()
        assertNotNull(screen)
    }
}
