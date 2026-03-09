package com.ecuflasher

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 50, 50, 50)

        val title = TextView(this)
        title.text = "ECUFlasher Pro"
        title.textSize = 26f

        val status = TextView(this)
        status.text = "Ready"
        status.textSize = 18f

        val connect = Button(this)
        connect.text = "Connect USB (Tactrix)"

        connect.setOnClickListener {
            status.text = "USB interface selected (Tactrix priority)"
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(connect)

        setContentView(layout)
    }
}
