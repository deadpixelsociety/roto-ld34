package com.thedeadpixelsociety.ld34.screens

import com.badlogic.gdx.Screen

interface GameScreen : Screen {
    val overlay: Boolean

    fun input()
    fun draw()
    fun update()
}
