package com.example.track_me_mobile.core.files

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSSearchPathDomainMask
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory

actual suspend fun saveExcelFile(fileName: String, bytes: ByteArray): Boolean {
    return try {
        val folder = NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory.NSDocumentDirectory, NSSearchPathDomainMask.NSUserDomainMask, true)
            .firstOrNull() as? NSString
            ?: NSString.create(string = NSTemporaryDirectory())
        val safeFileName = fileName.replace("[\\/:*?\"<>|]".toRegex(), "_")
        val path = folder.stringByAppendingPathComponent(safeFileName)

        val data = memScoped {
            NSData.create(bytes = bytes.toCValues(), length = bytes.size.toULong())
        } ?: return false

        data.writeToFile(path, true)
    } catch (e: Exception) {
        println("[SAVE_EXCEL] iOS save failed: ${e.message}")
        false
    }
}
