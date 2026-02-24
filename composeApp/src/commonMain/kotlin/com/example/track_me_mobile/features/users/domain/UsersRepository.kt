package com.example.track_me_mobile.features.users.domain

import com.example.track_me_mobile.features.users.data.model.PagedResponse
import com.example.track_me_mobile.features.users.data.model.UserDto

interface UsersRepository {
    suspend fun getTrackers(
        page: Int = 0,
        size: Int = 100,
        sort: List<String> = listOf("username,ASC"),
        showBlocked: Boolean = false
    ): Result<PagedResponse<UserDto>>

    suspend fun getAdministrators(
        page: Int = 0,
        size: Int = 100,
        sort: List<String> = listOf("username,ASC"),
        showBlocked: Boolean = false
    ): Result<PagedResponse<UserDto>>

    suspend fun getUserInfo(username: String): Result<UserDto>

    suspend fun enableUser(username: String): Result<Unit>
    suspend fun disableUser(username: String): Result<Unit>
}