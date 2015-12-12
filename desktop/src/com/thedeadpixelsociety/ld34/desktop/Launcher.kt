package com.thedeadpixelsociety.ld34.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.thedeadpixelsociety.ld34.LD34Game

fun main(args: Array<String>) {
    val config = LwjglApplicationConfiguration().apply {
        width = 1024
        height = 720
        title = "Ludum Dare 34"
    }

    LwjglApplication(LD34Game(), config)
}