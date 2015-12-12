package com.thedeadpixelsociety.ld34.screens

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.viewport.FitViewport
import com.thedeadpixelsociety.ld34.GameServices
import com.thedeadpixelsociety.ld34.TimeKeeper
import kotlin.properties.Delegates

class LevelScreen() : GameScreenImpl() {
    val batch by lazy { GameServices[SpriteBatch::class] }
    val debugRenderer by lazy { GameServices[Box2DDebugRenderer::class] }
    val renderer by lazy { GameServices[ShapeRenderer::class] }
    val camera = OrthographicCamera()
    val viewport = FitViewport(100f, 100f, camera)
    val world = World(Vector2(0f, 9.8f), false)
    var player by Delegates.notNull<Body>()

    override fun show() {
        val bodyDef = BodyDef()
        bodyDef.position.set(50f, 50f)
        bodyDef.type = BodyDef.BodyType.DynamicBody

        player = world.createBody(bodyDef)

        val fixtureDef = FixtureDef()
        fixtureDef.shape = CircleShape()
        fixtureDef.shape.radius = 1f
        fixtureDef.density = 1f

        player.createFixture(fixtureDef)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun update() {
        world.step(TimeKeeper.deltaTime, 1, 1)
    }

    override fun draw() {
        super.draw()

        viewport.apply()
        debugRenderer.render(world, viewport.camera.projection)
    }

    override fun dispose() {
        world.dispose()
    }
}
