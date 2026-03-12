package com.example.track_me_mobile.core.storage

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PersistentStorage {
    actual suspend fun saveString(key: String, value: String) {
    }

    actual suspend fun getString(key: String): String? {
        TODO("Not yet implemented")
    }

    actual suspend fun remove(key: String) {
    }

    actual companion object {
        actual fun create(): PersistentStorage {
            TODO("Not yet implemented")
        }
    }
}