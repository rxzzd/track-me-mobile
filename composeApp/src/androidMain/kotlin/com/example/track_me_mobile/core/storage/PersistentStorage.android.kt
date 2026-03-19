package com.example.track_me_mobile.core.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class PersistentStorage private constructor(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("trackme_prefs", Context.MODE_PRIVATE)
    }

    actual suspend fun saveString(key: String, value: String) {
        withContext(Dispatchers.IO) {
            prefs.edit().putString(key, value).apply()
        }
    }

    actual suspend fun getString(key: String): String? {
        return withContext(Dispatchers.IO) {
            prefs.getString(key, null)
        }
    }

    actual suspend fun remove(key: String) {
        withContext(Dispatchers.IO) {
            prefs.edit().remove(key).apply()
        }
    }

    actual companion object : KoinComponent {
        actual fun create(): PersistentStorage {
            val context: Context by inject()
            return PersistentStorage(context)
        }
    }
}