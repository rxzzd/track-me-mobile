package com.example.track_me_mobile.core.storage

expect class PersistentStorage {
    suspend fun saveString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)

    companion object {
        fun create(): PersistentStorage
    }
}