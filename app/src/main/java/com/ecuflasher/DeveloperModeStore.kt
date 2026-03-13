package com.ecuflasher

object DeveloperModeStore {

    @Volatile
    var enabled: Boolean = true
        private set

    @Synchronized
    fun setEnabled(value: Boolean) {
        enabled = value
    }

    @Synchronized
    fun toggle() {
        enabled = !enabled
    }
}
