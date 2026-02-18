package com.example.track_me_mobile.core.network

object ApiConstants {
    const val GATEWAY_HOST = "api.trackme.test.startup-poligon.com"
    const val SSO_HOST     = "sso.trackme.test.startup-poligon.com"

    private const val GATEWAY_BASE = "https://$GATEWAY_HOST"

    const val AUTH_TRIGGER_URL =
        "$GATEWAY_BASE/oauth2/authorization/track-me-client" +
                "?redirect_uri=$GATEWAY_BASE/login/oauth2/code/track-me-client"

    const val CSRF_ENDPOINT = "$GATEWAY_BASE/csrf"

    // Профиль идёт через шлюз с префиксом /sso/
    const val ACCOUNT_INFO = "$GATEWAY_BASE/sso/api/v1/account/info"
    const val ACCOUNT_UPDATE = "$GATEWAY_BASE/sso/api/v1/account/update"
}