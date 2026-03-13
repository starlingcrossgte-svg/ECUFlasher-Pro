package com.ecuflasher

import android.content.Context
import java.io.File

object LogFileWriter {

    private const val LOG_FILE_NAME = "ecuflasher_session_log.txt"

    @Synchronized
    fun append(context: Context, tag: String, message: String) {
        val file = File(context.filesDir, LOG_FILE_NAME)
        file.appendText("[$tag] $message\n")
    }

    @Synchronized
    fun readAll(context: Context): String {
        val file = File(context.filesDir, LOG_FILE_NAME)
        return if (file.exists()) file.readText() else "No saved log file yet"
    }

    @Synchronized
    fun clear(context: Context) {
        val file = File(context.filesDir, LOG_FILE_NAME)
        if (file.exists()) {
            file.writeText("")
        }
    }

    fun getLogFilePath(context: Context): String {
        return File(context.filesDir, LOG_FILE_NAME).absolutePath
    }
}
