package com.example.track_me_mobile.core.domain.models

data class UserInfo(
    val id: String,
    val username: String,
    val fullName: String?,
    val roles: List<Role>
) {
    val mainRole: Role
        get() = when {
            roles.contains(Role.SUPER_ADMIN) -> Role.SUPER_ADMIN
            roles.contains(Role.ADMIN) -> Role.ADMIN
            roles.contains(Role.TRACKER) -> Role.TRACKER
            else -> Role.UNKNOWN
        }
}