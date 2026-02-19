package com.example.track_me_mobile.core.network

object ApiConstants {
    const val GATEWAY_HOST = "api.trackme.test.startup-poligon.com"
    const val SSO_HOST     = "sso.trackme.test.startup-poligon.com"

    private const val GATEWAY_BASE = "https://$GATEWAY_HOST"

    // Auth
    const val AUTH_TRIGGER_URL =
        "$GATEWAY_BASE/oauth2/authorization/track-me-client" +
                "?redirect_uri=$GATEWAY_BASE/login/oauth2/code/track-me-client"
    const val CSRF_ENDPOINT   = "$GATEWAY_BASE/csrf"
    const val ACCOUNT_INFO    = "$GATEWAY_BASE/sso/api/v1/account/info"

    const val REGISTRATION_INIT = "$GATEWAY_BASE/sso/api/v1/registration/init"

    // Backend
    const val BACKEND_BASE = "$GATEWAY_BASE/backend"
}