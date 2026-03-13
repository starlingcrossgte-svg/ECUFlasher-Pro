package com.ecuflasher

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var refreshButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.usbStatusText)
        refreshButton = findViewById(R.id.refreshButton)

        refreshButton.setOnClickListener {

            val manager = UsbDeviceManager(this)

            val result = manager.openTactrixChannel()

            if (result) {
                statusText.text = "Tactrix communication test complete"
            } else {
                statusText.text = "Tactrix device not detected"
            }
        }

        EcuLogger.main("ECUFlasher started")
    }
}
