package com.thedeadpixelsociety.ld34.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Disposable
import com.thedeadpixelsociety.ld34.TimeKeeper
import java.util.*

class GameScreenService() : Disposable {
    private val screens = Stack<GameScreen>()
    private var accumulator = 0f

    fun <T : GameScreen> push(screen: T) {
        screens.push(screen)
        screen.show()
        screen.resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    private fun <T : GameScreen> remove(screen: T) {
        screens.remove(screen)
        screen.hide()
        screen.dispose()
    }

    private fun draw() {
        screens.forEach { it.draw() }
    }

    private fun update() {
        if (screens.size == 0) return

        var top = true
        for (i in 0..screens.size - 1) {
            val screen = screens[screens.size - 1 - i]

            if (top) {
                screen.input()
                screen.update()
            }

            if (!top) remove(screen)
            if (!screen.overlay) top = false
        }
    }

    fun pause() {
        screens.forEach { it.pause() }
    }

    fun resize(width: Int, height: Int) {
        screens.forEach { it.resize(width, height) }
    }

    fun render(delta: Float) {
        accumulator += Math.min(delta, TimeKeeper.MAX_DT)

        while (accumulator >= TimeKeeper.DT) {
            TimeKeeper.deltaTime = TimeKeeper.DT
            update()
            accumulator -= TimeKeeper.DT
        }

        draw()

        TimeKeeper.reset()
    }

    fun resume() {
        screens.forEach { it.resume() }
    }

    override fun dispose() {
        screens.forEach { it.dispose() }
    }
}
