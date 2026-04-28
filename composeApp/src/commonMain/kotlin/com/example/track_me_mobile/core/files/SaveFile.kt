package com.example.track_me_mobile.core.files

expect suspend fun saveExcelFile(fileName: String, bytes: ByteArray): Boolean
