package com.example.track_me_mobile.features.profile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountInfoDto(
    @SerialName("id")          val id: String,
    @SerialName("username")    val username: String,
    @SerialName("roles")       val roles: List<String>,
    @SerialName("fullName")    val fullName: String,
    @SerialName("email")       val email: String,
    @SerialName("phoneNumber") val phoneNumber: String? = null,
    @SerialName("avatarUrl")   val avatarUrl: String? = null,
    @SerialName("enabled")     val enabled: Boolean
)

@Serializable
data class UpdateAccountRequest(
    @SerialName("fullName")    val fullName: String,
    @SerialName("email")       val email: String,
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("avatarUrl")   val avatarUrl: String? = null
)