package com.ecuflasher

import android.content.Context
import android.hardware.usb.*

class UsbDeviceManager(private val context: Context) {

    companion object {
        private const val TACTRIX_VENDOR_ID = 1027
        private const val TACTRIX_PRODUCT_ID = 52301
    }

    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    fun openTactrixChannel(): Boolean {

        val deviceList = usbManager.deviceList

        for (device in deviceList.values) {

            if (device.vendorId == TACTRIX_VENDOR_ID &&
                device.productId == TACTRIX_PRODUCT_ID) {

                EcuLogger.usb("Opening Tactrix connection")

                val connection = usbManager.openDevice(device)
                    ?: return false

                val usbInterface = device.getInterface(1)

                if (!connection.claimInterface(usbInterface, true)) {
                    EcuLogger.usb("Failed to claim interface")
                    return false
                }

                val endpointOut = usbInterface.getEndpoint(0)
                val endpointIn = usbInterface.getEndpoint(1)

                EcuLogger.usb("Sending test packet")

                val testPacket = byteArrayOf(0x00)

                val sent = connection.bulkTransfer(
                    endpointOut,
                    testPacket,
                    testPacket.size,
                    1000
                )

                EcuLogger.usb("Bytes sent: $sent")

                val buffer = ByteArray(64)

                val received = connection.bulkTransfer(
                    endpointIn,
                    buffer,
                    buffer.size,
                    1000
                )

                EcuLogger.usb("Bytes received: $received")

                return true
            }
        }

        EcuLogger.usb("Tactrix device not found")
        return false
    }
}
