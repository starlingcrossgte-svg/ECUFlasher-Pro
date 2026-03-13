package com.ecuflasher

import android.content.Context

data class TactrixTestResult(
    val success: Boolean,
    val statusMessage: String,
    val bytesSent: Int,
    val bytesReceived: Int,
    val responseHex: String
)

class UsbDeviceManager(private val context: Context) {

    fun openTactrixChannel(): TactrixTestResult {
        val sessionRunner = OpenPortSessionRunner(context)
        return sessionRunner.runConnectionTest()
    }
}
