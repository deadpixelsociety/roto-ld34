package com.thedeadpixelsociety.ld34

object TimeKeeper {
    val DT = .01f
    val MAX_DT = 16.666f

    var frameTime = 0f
        get
        private set
    var totalTime = 0f
        get
        private set
    var deltaTime = 0f
        get
        set(value) {
            field = value
            frameTime += value
            totalTime += value
        }

    fun reset() {
        frameTime = 0f
    }
}
