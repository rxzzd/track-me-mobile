package com.example.track_me_mobile.core.network


object ApiConstants {
    // Для обычных запросов за данными оставляем /sso/ (как в Swagger)
    const val BASE_URL = "https://api.trackme.test.startup-poligon.com/sso/"

    // А вот для триггера авторизации убираем /sso/ перед /oauth2/
    // Мы берем ту ссылку, которая 100% сработала у тебя в браузере!
    const val AUTH_TRIGGER_URL = "https://api.trackme.test.startup-poligon.com/oauth2/authorization/track-me-client"

    const val CSRF_ENDPOINT = "${BASE_URL}api/csrf"
}