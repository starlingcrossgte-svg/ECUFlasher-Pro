package com.ecuflasher

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.ecuflasher.USB_PERMISSION"
    private val TACTRIX_VENDOR_ID = 1027
    private val TACTRIX_PRODUCT_ID = 52301

    private lateinit var statusText: TextView
    private lateinit var refreshButton: Button

    private lateinit var developerModeStatusText: TextView
    private lateinit var debugDetailsPanel: LinearLayout
    private lateinit var sessionSummaryPanel: LinearLayout
    private lateinit var manualCommandPanel: LinearLayout
    private lateinit var developerToolsPanel: LinearLayout

    private lateinit var deviceStateText: TextView
    private lateinit var permissionStateText: TextView
    private lateinit var lastCommandText: TextView
    private lateinit var bytesSentText: TextView
    private lateinit var bytesReceivedText: TextView
    private lateinit var responseHexText: TextView

    private lateinit var toggleDeveloperModeButton: Button
    private lateinit var developerLogText: TextView
    private lateinit var clearLogsButton: Button

    private lateinit var manualCommandPresetSpinner: Spinner
    private lateinit var manualCommandInput: EditText
    private lateinit var sendManualCommandButton: Button
    private lateinit var manualCommandResponseText: TextView

    private var developerModeEnabled = false

    private var lastCommand = "None"
    private var bytesSent = "-"
    private var bytesReceived = "-"
    private var responseHex = "--"

    private fun refreshDeveloperLog() {
        developerLogText.text = EcuLogger.getLogs()
    }

    private fun setDeveloperPanelsVisible(visible: Boolean) {
        val state = if (visible) View.VISIBLE else View.GONE
        debugDetailsPanel.visibility = state
        sessionSummaryPanel.visibility = state
        manualCommandPanel.visibility = state
        developerToolsPanel.visibility = state
    }

    private fun getTactrixDevice(systemUsbManager: UsbManager): UsbDevice? {
        return systemUsbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }
    }

    private fun refreshDebugPanel() {
        val systemUsbManager = getSystemService(USB_SERVICE) as UsbManager
        val tactrixDevice = getTactrixDevice(systemUsbManager)

        if (tactrixDevice == null) {
            deviceStateText.text = "Device: Not detected"
            permissionStateText.text = "Permission: Not applicable"
        } else {
            deviceStateText.text = "Device: Tactrix OpenPort detected"
            permissionStateText.text = if (systemUsbManager.hasPermission(tactrixDevice)) {
                "Permission: Granted"
            } else {
                "Permission: Not granted"
            }
        }

        lastCommandText.text = "Last Command: $lastCommand"
        bytesSentText.text = "Bytes Sent: $bytesSent"
        bytesReceivedText.text = "Bytes Received: $bytesReceived"
        responseHexText.text = "Response Hex: $responseHex"
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_USB_PERMISSION) return

            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

            if (granted) {
                EcuLogger.usb("USB permission granted")
                statusText.text = "OpenPort detected and permission granted"
            } else {
                EcuLogger.usb("USB permission denied")
                statusText.text = "USB permission denied"
            }

            refreshDeveloperLog()
            refreshDebugPanel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusMessageText)
        refreshButton = findViewById(R.id.refreshButton)

        developerModeStatusText = findViewById(R.id.developerModeStatusText)

        debugDetailsPanel = findViewById(R.id.debugDetailsPanel)
        sessionSummaryPanel = findViewById(R.id.sessionSummaryPanel)
        manualCommandPanel = findViewById(R.id.manualCommandPanel)
        developerToolsPanel = findViewById(R.id.liveLogPanel)

        deviceStateText = findViewById(R.id.deviceStateText)
        permissionStateText = findViewById(R.id.permissionStateText)
        lastCommandText = findViewById(R.id.lastCommandText)
        bytesSentText = findViewById(R.id.bytesSentText)
        bytesReceivedText = findViewById(R.id.bytesReceivedText)
        responseHexText = findViewById(R.id.responseHexText)

        toggleDeveloperModeButton = findViewById(R.id.toggleDeveloperModeButton)
        developerLogText = findViewById(R.id.liveLogText)
        clearLogsButton = findViewById(R.id.clearLogsButton)

        manualCommandPresetSpinner = findViewById(R.id.manualCommandPresetSpinner)
        manualCommandInput = findViewById(R.id.manualCommandInput)
        sendManualCommandButton = findViewById(R.id.sendManualCommandButton)
        manualCommandResponseText = findViewById(R.id.manualCommandResponseText)

        registerReceiver(
            usbReceiver,
            IntentFilter(ACTION_USB_PERMISSION),
            Context.RECEIVER_NOT_EXPORTED
        )

        refreshButton.setOnClickListener {
            checkTactrix()
            refreshDeveloperLog()
            refreshDebugPanel()
        }

        toggleDeveloperModeButton.setOnClickListener {
            developerModeEnabled = !developerModeEnabled

            if (developerModeEnabled) {
                developerModeStatusText.text = "Developer Mode: ON"
                setDeveloperPanelsVisible(true)
                EcuLogger.main("Developer mode enabled")
            } else {
                developerModeStatusText.text = "Developer Mode: OFF"
                setDeveloperPanelsVisible(false)
                EcuLogger.main("Developer mode disabled")
            }

            refreshDeveloperLog()
            refreshDebugPanel()
        }

        clearLogsButton.setOnClickListener {
            EcuLogger.clear()
            refreshDeveloperLog()
        }

        sendManualCommandButton.setOnClickListener {
            val command = manualCommandInput.text.toString().trim()

            if (command.isEmpty()) {
                manualCommandResponseText.text = "No command entered"
                return@setOnClickListener
            }

            lastCommand = command
            bytesSent = command.length.toString()
            bytesReceived = "-"
            responseHex = "--"

            manualCommandResponseText.text = "Command queued: $command"
            EcuLogger.main("Manual command sent: $command")

            refreshDeveloperLog()
            refreshDebugPanel()
        }

        setupCommandPresets()

        developerModeEnabled = false
        developerModeStatusText.text = "Developer Mode: OFF"
        setDeveloperPanelsVisible(false)

        statusText.text = "USB Status Unknown"

        EcuLogger.main("HashSlingingFlasher started")
        refreshDeveloperLog()
        refreshDebugPanel()
    }

    private fun setupCommandPresets() {
        val presets = listOf(
            "Select Command",
            "ATI",
            "ATZ",
            "ATE0",
            "ATH1",
            "ATSP0"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            presets
        )

        manualCommandPresetSpinner.adapter = adapter

        manualCommandPresetSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) return

                    val command = presets[position]
                    manualCommandInput.setText(command)
                    EcuLogger.main("Preset selected: $command")
                    refreshDeveloperLog()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    private fun checkTactrix() {
        val systemUsbManager = getSystemService(USB_SERVICE) as UsbManager
        val tactrixDevice = getTactrixDevice(systemUsbManager)

        if (tactrixDevice == null) {
            EcuLogger.usb("Tactrix device not found")
            statusText.text = "Tactrix device not detected"
            refreshDebugPanel()
            return
        }

        if (!systemUsbManager.hasPermission(tactrixDevice)) {
            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_IMMUTABLE
            )

            EcuLogger.usb("Requested USB permission for Tactrix")
            statusText.text = "Requesting USB permission..."
            systemUsbManager.requestPermission(tactrixDevice, permissionIntent)
            refreshDebugPanel()
            return
        }

        EcuLogger.usb("USB permission already granted")
        statusText.text = "OpenPort detected and permission granted"
        refreshDebugPanel()
    }
}
