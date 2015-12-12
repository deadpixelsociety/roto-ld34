package com.thedeadpixelsociety.ld34

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.thedeadpixelsociety.ld34.screens.GameScreenService
import com.thedeadpixelsociety.ld34.screens.LevelScreen

class LD34Game() : ApplicationAdapter() {
    val screenService = GameScreenService()

    override fun create() {
        GameServices.put(AssetManager())
        GameServices.put(ShapeRenderer())
        GameServices.put(SpriteBatch())
        GameServices.put(Box2DDebugRenderer())

        screenService.push(LevelScreen())
    }

    override fun resize(width: Int, height: Int) {
        screenService.resize(width, height)
    }

    override fun render() {
        screenService.render(Gdx.graphics.deltaTime)
    }

    override fun pause() {
        screenService.pause()
    }

    override fun resume() {
        screenService.resume()
    }

    override fun dispose() {
        screenService.dispose()
        GameServices.dispose()
    }
}
