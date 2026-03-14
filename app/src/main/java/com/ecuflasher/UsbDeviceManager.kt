package com.ecuflasher

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.content.Context

data class TactrixTestResult(
    val success: Boolean,
    val statusMessage: String
)

class UsbDeviceManager(private val context: Context) {

    private val TACTRIX_VENDOR_ID = 1027
    private val TACTRIX_PRODUCT_ID = 52301
    private val READ_TIMEOUT_MS = 4000
    private val WRITE_TIMEOUT_MS = 3000

    fun openTactrixChannel(): TactrixTestResult {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val tactrixDevice: UsbDevice? = usbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }

        if (tactrixDevice == null) {
            return TactrixTestResult(false, "[ERROR] No Tactrix USB device detected")
        }

        val connection: UsbDeviceConnection? = usbManager.openDevice(tactrixDevice)
        if (connection == null) {
            return TactrixTestResult(false, "[ERROR] Failed to open Tactrix USB device. USB permission granted? ${usbManager.hasPermission(tactrixDevice)}")
        }

        val usbInterface: UsbInterface? = tactrixDevice.getInterface(0)
        if (usbInterface == null) {
            connection.close()
            return TactrixTestResult(false, "[ERROR] No interfaces found on Tactrix device")
        }

        val claimed = connection.claimInterface(usbInterface, true)
        if (!claimed) {
            connection.close()
            return TactrixTestResult(false, "[ERROR] Failed to claim Tactrix interface. Is it in use by another app?")
        }

        val endpointOut: UsbEndpoint? = (0 until usbInterface.endpointCount)
            .map { usbInterface.getEndpoint(it) }
            .firstOrNull { it.direction == UsbEndpoint.DIRECTION_OUT }

        val endpointIn: UsbEndpoint? = (0 until usbInterface.endpointCount)
            .map { usbInterface.getEndpoint(it) }
            .firstOrNull { it.direction == UsbEndpoint.DIRECTION_IN }

        if (endpointOut == null || endpointIn == null) {
            connection.releaseInterface(usbInterface)
            connection.close()
            return TactrixTestResult(false, "[ERROR] Failed to find bulk endpoints. OUT: $endpointOut IN: $endpointIn")
        }

        return TactrixTestResult(true, "Tactrix USB interface opened successfully. OUT: ${endpointOut.address} IN: ${endpointIn.address}")
    }
}

fun sendManualAsciiCommand(command: String): String {
    val tDevice = getTactrixDevice(context)
    if (tDevice == null) return "[ERROR] No Tactrix device detected"

    val connection: UsbDeviceConnection? = context.getSystemService(Context.USB_SERVICE)
        ?.let { (it as UsbManager).openDevice(tDevice) }

    if (connection == null) return "[ERROR] Failed to open Tactrix USB device"

    val usbInterface: UsbInterface? = tDevice.getInterface(0)
    if (usbInterface == null) {
        connection.close()
        return "[ERROR] No interfaces found on Tactrix device"
    }

    val claimed = connection.claimInterface(usbInterface, true)
    if (!claimed) {
        connection.close()
        return "[ERROR] Failed to claim Tactrix interface"
    }

    val endpointOut: UsbEndpoint? = (0 until usbInterface.endpointCount)
        .map { usbInterface.getEndpoint(it) }
        .firstOrNull { it.direction == UsbEndpoint.DIRECTION_OUT }

    val endpointIn: UsbEndpoint? = (0 until usbInterface.endpointCount)
        .map { usbInterface.getEndpoint(it) }
        .firstOrNull { it.direction == UsbEndpoint.DIRECTION_IN }

    if (endpointOut == null || endpointIn == null) {
        connection.releaseInterface(usbInterface)
        connection.close()
        return "[ERROR] Failed to find bulk endpoints. OUT: $endpointOut IN: $endpointIn"
    }

    val bytes = command.toByteArray()
    connection.bulkTransfer(endpointOut, bytes, bytes.size, WRITE_TIMEOUT_MS)

    val responseBuffer = ByteArray(1024)
    val read = connection.bulkTransfer(endpointIn, responseBuffer, responseBuffer.size, READ_TIMEOUT_MS)

    connection.releaseInterface(usbInterface)
    connection.close()

    return if (read >= 0) String(responseBuffer, 0, read) else "[ERROR] No response"
}
