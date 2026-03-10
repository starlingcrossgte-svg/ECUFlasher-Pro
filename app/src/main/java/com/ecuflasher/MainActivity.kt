package com.ecuflasher

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbDevice

class MainActivity : Activity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusText = TextView(this)
        statusText.text = "Initializing ECUFlasher..."
        setContentView(statusText)

        checkUsbDevices()
    }

    private fun checkUsbDevices() {
        val usbManager = getSystemService(USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList

        if (deviceList.isEmpty()) {
            statusText.text = "No USB devices connected"
        } else {
            val builder = StringBuilder("USB Devices connected:\n")
            deviceList.values.forEach { device ->
                builder.append("Vendor ID: ${device.vendorId}, Product ID: ${device.productId}\n")
            }
            statusText.text = builder.toString()
        }
    }
}
