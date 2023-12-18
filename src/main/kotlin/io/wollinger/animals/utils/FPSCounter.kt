package io.wollinger.animals.utils

import kotlin.js.Date

class FPSCounter {
    private var printFps = "Calculating..."
    private var fps = 0
    private var lastFPS = Date.now()

    fun frame() {
        val now = Date.now()
        if(lastFPS + 1000.0 < now) {
            printFps = "$fps"
            lastFPS = now
            fps = 0
        }
        fps++
    }

    fun getString() = printFps
}