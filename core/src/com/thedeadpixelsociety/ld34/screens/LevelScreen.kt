package com.thedeadpixelsociety.ld34.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.thedeadpixelsociety.ld34.GameServices
import com.thedeadpixelsociety.ld34.TimeKeeper
import com.thedeadpixelsociety.ld34.components.Box2DComponent
import com.thedeadpixelsociety.ld34.graphics.Palette
import com.thedeadpixelsociety.ld34.systems.Box2DSystem
import com.thedeadpixelsociety.ld34.systems.RenderSystem
import com.thedeadpixelsociety.ld34.systems.ScriptSystem
import com.thedeadpixelsociety.ld34.systems.TagSystem
import com.thedeadpixelsociety.ld34.using
import com.thedeadpixelsociety.ld34.world.createEntitiesFromMapLayer
import kotlin.properties.Delegates

class LevelScreen(val levelName: String) : GameScreenImpl() {
    companion object {
        const val DEBUG_BOX2D = true
        const val ROTATE_FACTOR = 200f
    }

    val debugRenderer by lazy { GameServices[Box2DDebugRenderer::class] }
    val camera = OrthographicCamera()
    var viewport by Delegates.notNull<Viewport>()
    val engine = Engine()

    override fun show() {
        camera.zoom = .3f
        clearColor = Palette.COUP_DE_GRACE

        viewport = ExtendViewport(1600f, 1600f, camera)
        engine.addSystem(TagSystem())
        engine.addSystem(Box2DSystem(Vector2(0f, -100f)))
        engine.addSystem(ScriptSystem())
        engine.addSystem(RenderSystem(viewport))

        val loader = TmxMapLoader()
        val map = loader.load("levels/$levelName.tmx")

        map.using {
            val levelLayer = it.layers.get("level") ?: throw IllegalStateException("Layer 'level' not found.")
            createEntitiesFromMapLayer(levelLayer, engine)
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun input() {
        var direction = 0
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            direction--
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            direction++
        }

        if (direction != 0) {
            val angle = ROTATE_FACTOR * direction * TimeKeeper.deltaTime

            val system = engine.getSystem(Box2DSystem::class.java)
            system.world.gravity = system.world.gravity.rotate(angle)
            camera.rotate(angle, 0f, 0f, 1f)
        }
    }

    override fun update() {
        engine.update(TimeKeeper.deltaTime)
        val player = engine.getSystem(TagSystem::class.java)["player"]
        camera.position.set(player?.getComponent(Box2DComponent::class.java)?.body?.position ?: Vector2.Zero, 0f)
    }

    override fun draw() {
        super.draw()

        engine.getSystem(RenderSystem::class.java).update(TimeKeeper.frameTime)

        if (DEBUG_BOX2D) {
            debugRenderer.render(engine.getSystem(Box2DSystem::class.java).world, viewport.camera.combined)
        }
    }

    override fun dispose() {
        engine.systems.forEach { engine.removeSystem(it) }
    }
}
