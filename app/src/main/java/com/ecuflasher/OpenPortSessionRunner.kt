package com.ecuflasher

import android.content.Context

class OpenPortSessionRunner(private val context: Context) {

    fun runConnectionTest(): TactrixTestResult {
        val usbTransport = UsbTransport(context)
        val openResult = usbTransport.openTactrix()
            ?: return TactrixTestResult(false, "Tactrix device not detected", -1, -1, "")

        val openPortClient = OpenPortClient(
            connection = openResult.connection,
            endpointOut = openResult.endpointOut,
            endpointIn = openResult.endpointIn
        )

        val ataResult = openPortClient.openCommandChannel()

        if (!ataResult.responseAscii.contains("aro")) {
            usbTransport.close(openResult)
            return TactrixTestResult(
                false,
                "OpenPort ATA failed before ECU query",
                ataResult.bytesSent,
                ataResult.bytesReceived,
                ataResult.responseHex
            )
        }

        val atoResult = openPortClient.openCanBus500k()

        if (!atoResult.responseAscii.contains("aro")) {
            usbTransport.close(openResult)
            return TactrixTestResult(
                false,
                "OpenPort CAN bus open failed before ECU query",
                atoResult.bytesSent,
                atoResult.bytesReceived,
                atoResult.responseHex
            )
        }

        val ecuResult = openPortClient.sendObdCanQuery0100()

        usbTransport.close(openResult)

        val success = containsSequence(
            ecuResult.responseBytes,
            byteArrayOf(0x41, 0x00)
        )

        return TactrixTestResult(
            success = success,
            statusMessage = if (success) {
                "ECU response received"
            } else if (ecuResult.bytesReceived > 0) {
                "Raw vehicle response received"
            } else {
                "ECU query sent but no response"
            },
            bytesSent = ecuResult.bytesSent,
            bytesReceived = ecuResult.bytesReceived,
            responseHex = ecuResult.responseHex
        )
    }

    private fun containsSequence(data: ByteArray, pattern: ByteArray): Boolean {
        if (pattern.isEmpty() || data.size < pattern.size) return false

        for (i in 0..data.size - pattern.size) {
            var match = true
            for (j in pattern.indices) {
                if (data[i + j] != pattern[j]) {
                    match = false
                    break
                }
            }
            if (match) return true
        }

        return false
    }
}
