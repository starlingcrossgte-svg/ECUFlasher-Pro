package com.ecuflasher

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val statusText = TextView(this).apply {
            text = "ECUFlasher Pro Ready"
            textSize = 24f
        }

        val connectButton = Button(this).apply {
            text = "Connect USB (Tactrix)"
            setOnClickListener {
                statusText.text = "USB connection selected (Tactrix priority)"
            }
        }

        layout.addView(statusText)
        layout.addView(connectButton)

        setContentView(layout)
    }
}
