package com.thedeadpixelsociety.ld34.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.Timer
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.thedeadpixelsociety.ld34.*
import com.thedeadpixelsociety.ld34.components.Box2DComponent
import com.thedeadpixelsociety.ld34.components.TransformComponent
import com.thedeadpixelsociety.ld34.graphics.Palette
import com.thedeadpixelsociety.ld34.systems.Box2DSystem
import com.thedeadpixelsociety.ld34.systems.RenderSystem
import com.thedeadpixelsociety.ld34.systems.ScriptSystem
import com.thedeadpixelsociety.ld34.systems.TagSystem
import kotlin.properties.Delegates

class LevelScreen(val levelName: String) : GameScreenImpl() {
    companion object {
        const val DEBUG_BOX2D = false
        const val GRAVITY_STRENGTH = -175f
        const val ROTATE_FACTOR = 150f
        const val TRANSITION_OUT_DELAY = .5f
        const val TRANSITION_TIME = 1f
    }

    val debugRenderer by lazy { GameServices[Box2DDebugRenderer::class] }
    val renderer by lazy { GameServices[ShapeRenderer::class] }
    var camera by Delegates.notNull<OrthographicCamera>()
    val uiCamera = OrthographicCamera()
    var viewport by Delegates.notNull<Viewport>()
    val engine = Engine()
    var transitionOut = false
    var transitionIn = true
    val transitionFrom = Vector2()
    var transitionRadius = 0f
    var finished = false

    override fun show() {
        clearColor = Palette.COUP_DE_GRACE

        val loader = TmxMapLoader()
        val map = loader.load("levels/$levelName.tmx")
        val w = map.properties["width"] as Int
        val h = map.properties["height"] as Int
        val tw = map.properties["tilewidth"] as Int
        val th = map.properties["tileheight"] as Int
        val gravity = if (map.properties.containsKey("gravity")) (map.properties["gravity"] as String).toFloat() else GRAVITY_STRENGTH

        viewport = ExtendViewport((w * tw).toFloat(), (h * th).toFloat())
        camera = viewport.camera as OrthographicCamera
        camera.zoom = .3f

        viewport.update(Gdx.graphics.width, Gdx.graphics.height)

        engine.addSystem(TagSystem())
        engine.addSystem(Box2DSystem(Vector2(0f, gravity)))
        engine.addSystem(ScriptSystem())
        engine.addSystem(RenderSystem(viewport))

        map.using {
            val levelLayer = it.layers.get("level") ?: throw IllegalStateException("Layer 'level' not found.")
            createEntitiesFromMapLayer(levelLayer, engine)
        }

        if (transitionIn) {
            // Pump the engine once to settle everything in place
            engine.update(TimeKeeper.DT)

            val goal = engine.getSystem(TagSystem::class.java)["player"]
            if (goal != null) {
                val position = goal.getComponent(TransformComponent::class.java)?.position ?: Vector2.Zero
                transitionFrom.set(position)
            }

            transitionRadius = viewport.worldWidth
            t = 0f
        }

        Events.goal = { onGoal() }
        Events.dead = { onDead() }
    }

    private fun onDead() {
        println("fucking dead m8")
        LevelManager.retry()
    }

    private fun onGoal() {
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                val goal = engine.getSystem(TagSystem::class.java)["player"]
                if (goal != null) {
                    val position = goal.getComponent(TransformComponent::class.java)?.position ?: Vector2.Zero
                    transitionFrom.set(position)
                }

                transitionRadius = 0f
                t = 0f
                transitionOut = true
            }
        }, TRANSITION_OUT_DELAY)
    }

    override fun resize(width: Int, height: Int) {
        uiCamera.setToOrtho(false, width.toFloat(), height.toFloat())
        viewport.update(width, height)
    }

    override fun input() {
        if (!transitionOut && !transitionIn) {
            var direction = 0
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                direction--
            }

            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                direction++
            }

            if (direction != 0) {
                val angle = ROTATE_FACTOR * direction * TimeKeeper.deltaTime
                engine.getSystem(RenderSystem::class.java).angle = angle

                val system = engine.getSystem(Box2DSystem::class.java)
                system.world.gravity = system.world.gravity.rotate(angle)
                camera.rotate(angle, 0f, 0f, 1f)
            }
        }
    }

    override fun update() {
        val player = engine.getSystem(TagSystem::class.java)["player"]
        camera.position.set(player?.getComponent(Box2DComponent::class.java)?.body?.position ?: Vector2.Zero, 0f)

        if (!transitionOut && !transitionIn) {
            engine.update(TimeKeeper.deltaTime)
        } else if (transitionOut) {
            t += TimeKeeper.deltaTime
            if (transitionRadius > Gdx.graphics.width && !finished) {
                finished = true
                LevelManager.next()
            }
        } else if (transitionIn) {
            t += TimeKeeper.deltaTime
            if (transitionRadius <= 0f) {
                transitionIn = false
            }
        }
    }

    override fun draw() {
        super.draw()

        engine.getSystem(RenderSystem::class.java).update(TimeKeeper.frameTime)

        if (DEBUG_BOX2D) {
            debugRenderer.render(engine.getSystem(Box2DSystem::class.java).world, viewport.camera.combined)
        }

        if (transitionOut && !finished) {
            renderTransition(1)
        } else if (transitionIn) {
            renderTransition(-1)
        }
    }

    var t = 0f
    private fun renderTransition(dir: Int) {
        if (dir == -1) {
            transitionRadius = Interpolation.bounce.apply(viewport.worldWidth, 0f, t / TRANSITION_TIME)
        } else {
            transitionRadius = Interpolation.fade.apply(0f, viewport.worldWidth, t / TRANSITION_TIME)
        }

        renderer.projectionMatrix = viewport.camera.combined
        renderer.begin(ShapeRenderer.ShapeType.Filled)

        renderer.color = Palette.COMFORTABLE_SKIN
        renderer.circle(transitionFrom.x, transitionFrom.y, transitionRadius, 64)

        renderer.end()
    }

    override fun dispose() {
        engine.systems.forEach { engine.removeSystem(it) }
    }
}
