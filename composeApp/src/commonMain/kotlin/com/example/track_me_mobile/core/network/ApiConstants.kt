package com.example.track_me_mobile.core.network

object ApiConstants {
    const val GATEWAY_HOST = "api.trackme.test.startup-poligon.com"
    const val SSO_HOST     = "sso.trackme.test.startup-poligon.com"

    private const val GATEWAY_BASE = "https://$GATEWAY_HOST"
    val STREAMS_ENDPOINT = "https://$GATEWAY_HOST/backend/api/v1/admin/streams"
    val NTI_MARKETS_ENDPOINT = "https://$GATEWAY_HOST/backend/api/v1/streams/nti-markets"
    const val AUTH_TRIGGER_URL =
        "$GATEWAY_BASE/oauth2/authorization/track-me-client" +
                "?redirect_uri=$GATEWAY_BASE/login/oauth2/code/track-me-client"

    const val CSRF_ENDPOINT = "$GATEWAY_BASE/csrf"

    // Профиль идёт через шлюз с префиксом /sso/
    const val ACCOUNT_INFO = "$GATEWAY_BASE/sso/api/v1/account/info"
    const val ACCOUNT_UPDATE = "$GATEWAY_BASE/sso/api/v1/account/update"

    const val USERS_TRACKERS        = "$GATEWAY_BASE/sso/api/v1/users/trackers"
    const val USERS_ADMINISTRATORS  = "$GATEWAY_BASE/sso/api/v1/users/administrators"
    const val USERS_INFO            = "$GATEWAY_BASE/sso/api/v1/users" // + /{username}/info
    const val USERS_ENABLE          = "$GATEWAY_BASE/sso/api/v1/users/enable"
    const val USERS_DISABLE         = "$GATEWAY_BASE/sso/api/v1/users/disable"

}