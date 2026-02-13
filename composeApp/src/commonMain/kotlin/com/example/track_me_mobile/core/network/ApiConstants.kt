package com.example.track_me_mobile.core.network

object ApiConstants {
    const val BASE_URL = "https://api.trackme.test.startup-poligon.com/"
    const val SSO_URL = "https://sso.trackme.test.startup-poligon.com/"

    // Эндпоинты
    const val CSRF_ENDPOINT = "${SSO_URL}api/csrf"
    const val LOGIN_ENDPOINT = "${SSO_URL}client/login"
}