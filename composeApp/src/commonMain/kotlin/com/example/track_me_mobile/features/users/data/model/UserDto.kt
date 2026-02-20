package com.example.track_me_mobile.features.users.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")          val id: String?,
    @SerialName("username")    val username: String,
    @SerialName("roles")       val roles: List<String>,
    @SerialName("fullName")    val fullName: String,
    @SerialName("email")       val email: String,
    @SerialName("phoneNumber") val phoneNumber: String?,
    @SerialName("avatarUrl")   val avatarUrl: String?,
    @SerialName("enabled")     val enabled: Boolean
)

@Serializable
data class PagedResponse<T>(
    @SerialName("content")       val content: List<T>,
    @SerialName("page")          val page: PageInfo
)

@Serializable
data class PageInfo(
    @SerialName("size")          val size: Int,
    @SerialName("number")        val number: Int,
    @SerialName("totalElements") val totalElements: Long,
    @SerialName("totalPages")    val totalPages: Int
)

@Serializable
data class UsersRequest(
    @SerialName("filters") val filters: List<FilterCriteria> = emptyList()
)

@Serializable
data class FilterCriteria(
    @SerialName("fieldName") val fieldName: String,
    @SerialName("type")      val type: String,
    @SerialName("values")    val values: List<String>,
    @SerialName("value")     val value: String? = null
)