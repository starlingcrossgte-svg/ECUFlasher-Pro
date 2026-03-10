package com.ecuflasher

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ecuflasher.comm.usb.UsbSerialManager

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var usbManager: UsbManager
    private lateinit var usbSerialManager: UsbSerialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        usbManager = getSystemService(USB_SERVICE) as UsbManager
        usbSerialManager = UsbSerialManager(this)

        checkUsbDevices()
    }

    private fun checkUsbDevices() {
        val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList

        if (deviceList.isEmpty()) {
            statusText.text = "No USB devices connected"
        } else {
            val firstDevice = deviceList.values.first()
            statusText.text =
                "USB device detected\nVendor ID: ${firstDevice.vendorId}\nProduct ID: ${firstDevice.productId}"

            usbSerialManager.checkConnectedDevices()
        }
    }
}
