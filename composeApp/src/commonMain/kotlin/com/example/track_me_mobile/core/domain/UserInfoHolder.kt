package com.example.track_me_mobile.core.domain

import com.example.track_me_mobile.core.domain.models.UserInfo

// Хранит UserInfo после успешного входа.
// Живёт как single в Koin — один экземпляр на всё приложение.
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