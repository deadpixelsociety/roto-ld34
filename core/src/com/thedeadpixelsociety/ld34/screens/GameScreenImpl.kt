package com.thedeadpixelsociety.ld34.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20

abstract class GameScreenImpl() : GameScreen {
    override val overlay = false
    protected var clearColor = Color(Color.BLACK)
        get
        set(value) {
            field = Color(value)
        }

    override fun show() {
    }

    override fun hide() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun input() {
    }

    override fun update() {
    }

    override fun draw() {
        Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
    }

    override fun render(delta: Float) {
        throw UnsupportedOperationException()
    }
}