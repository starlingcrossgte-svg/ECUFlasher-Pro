package com.hashslingingflasher

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example startup code
        Toast.makeText(this, "HashSlingingFlasher started!", Toast.LENGTH_SHORT).show()
    }

    // Add your existing functions here, fully migrated to com.hashslingingflasher
}
