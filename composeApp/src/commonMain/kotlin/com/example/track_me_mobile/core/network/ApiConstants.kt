package com.example.track_me_mobile.core.network

object ApiConstants {
    const val GATEWAY_HOST = "api.trackme.test.startup-poligon.com"
    const val SSO_HOST     = "sso.trackme.test.startup-poligon.com"

    private const val GATEWAY_BASE = "https://$GATEWAY_HOST"

    // Streams
    val STREAMS_ENDPOINT         = "https://$GATEWAY_HOST/backend/api/v1/admin/streams"  // POST, ADMIN
    val STREAMS_PUBLIC_ENDPOINT  = "https://$GATEWAY_HOST/backend/api/v1/streams"         // POST, TRACKER
    val CREATE_STREAM_ENDPOINT   = "https://$GATEWAY_HOST/backend/api/v1/admin/streams"
    val NTI_MARKETS_ENDPOINT     = "https://$GATEWAY_HOST/backend/api/v1/streams/nti-markets"

    // Auth
    const val AUTH_TRIGGER_URL =
        "$GATEWAY_BASE/oauth2/authorization/track-me-client" +
                "?redirect_uri=$GATEWAY_BASE/login/oauth2/code/track-me-client"
    const val CSRF_ENDPOINT   = "$GATEWAY_BASE/csrf"
    const val ACCOUNT_INFO    = "$GATEWAY_BASE/sso/api/v1/account/info"

    const val REGISTRATION_INIT = "$GATEWAY_BASE/sso/api/v1/registration/init"
    const val ACCOUNT_UPDATE    = "$GATEWAY_BASE/sso/api/v1/account/update"

    // Backend
    const val BACKEND_BASE = "$GATEWAY_BASE/backend"

    // Users
    const val USERS_TRACKERS        = "$GATEWAY_BASE/sso/api/v1/users/trackers"
    const val USERS_ADMINISTRATORS  = "$GATEWAY_BASE/sso/api/v1/users/administrators"
    const val USERS_INFO            = "$GATEWAY_BASE/sso/api/v1/users"
    const val USERS_ENABLE          = "$GATEWAY_BASE/sso/api/v1/users/enable"
    const val USERS_DISABLE         = "$GATEWAY_BASE/sso/api/v1/users/disable"

    // Team card
    const val TEAM_CARD        = "$BACKEND_BASE/api/v1/team-card"        // TRACKER: PATCH/DELETE
    const val TEAM_CARD_ADMIN  = "$BACKEND_BASE/api/v1/admin/team-card"  // ADMIN: PATCH/DELETE
    const val TEAM_CARD_COUNT  = "$BACKEND_BASE/api/v1/team-card/count"
}