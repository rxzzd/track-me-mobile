package com.example.track_me_mobile.core.domain

import com.example.track_me_mobile.core.domain.models.UserInfo

class UserInfoHolder {
    var userInfo: UserInfo? = null
        private set

    fun save(info: UserInfo) {
        userInfo = info
    }

    fun clear() {
        userInfo = null
    }
}