package com.example.track_me_mobile.features.team_card.data.models

import com.example.track_me_mobile.features.team_card.domain.models.TrackerUser
import kotlinx.serialization.Serializable

@Serializable
data class TrackerUserDto(
    val id: String,
    val username: String,
    val fullName: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val roles: List<String> = emptyList(),
    val phoneNumber: String? = null,
    val enabled: Boolean = true
) {
    fun toDomain() = TrackerUser(
        id        = id,
        username  = username,
        fullName  = fullName ?: username,
        email     = email ?: "",
        avatarUrl = avatarUrl
    )
}

@Serializable
data class TrackersPageDto(
    val content: List<TrackerUserDto> = emptyList()
)