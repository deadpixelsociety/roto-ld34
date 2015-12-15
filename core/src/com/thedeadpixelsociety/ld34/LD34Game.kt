package com.thedeadpixelsociety.ld34

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.Timer
import com.thedeadpixelsociety.ld34.screens.GameScreenService
import com.thedeadpixelsociety.ld34.screens.LevelScreen

class LD34Game() : ApplicationAdapter() {
    val screenService = GameScreenService()

    override fun create() {
        createFonts()
        loadSounds()

        GameServices.put(AssetManager())
        GameServices.put(ShapeRenderer())
        GameServices.put(SpriteBatch())
        GameServices.put(Box2DDebugRenderer())
        GameServices.put(screenService)

        Timer.schedule(object : Timer.Task() {
            override fun run() {
                screenService.push(LevelScreen("-1"))
            }
        }, 1f)
    }

    private fun loadSounds() {
        Sounds.coin = Gdx.audio.newSound(Gdx.files.internal("sounds/coin.wav"))
        Sounds.bounce = Gdx.audio.newSound(Gdx.files.internal("sounds/bounce.wav"))
        Sounds.dead = Gdx.audio.newSound(Gdx.files.internal("sounds/dead.wav"))
    }

    private fun createFonts() {
        val gen = FreeTypeFontGenerator(Gdx.files.internal("fonts/PrintClearly.otf"))
        Fonts.font32 = gen.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 32
            color = Color.WHITE
        })

        gen.dispose()
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
        Fonts.font32.dispose()
        Sounds.dispose()
    }
}