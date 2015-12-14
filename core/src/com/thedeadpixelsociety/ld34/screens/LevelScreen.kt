package com.thedeadpixelsociety.ld34.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.Timer
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.thedeadpixelsociety.ld34.*
import com.thedeadpixelsociety.ld34.components.Box2DComponent
import com.thedeadpixelsociety.ld34.components.TransformComponent
import com.thedeadpixelsociety.ld34.graphics.Palette
import com.thedeadpixelsociety.ld34.systems.*
import kotlin.properties.Delegates

class LevelScreen(val levelName: String) : GameScreenImpl() {
    companion object {
        const val DEBUG_BOX2D = false
        const val GRAVITY_STRENGTH = -125f
        const val ROTATE_FACTOR = 150f
        const val TRANSITION_OUT_DELAY = .5f
        const val TRANSITION_TIME = 1f
    }

    private val batch by lazy { GameServices[SpriteBatch::class] }
    private val debugRenderer by lazy { GameServices[Box2DDebugRenderer::class] }
    private val renderer by lazy { GameServices[ShapeRenderer::class] }
    private var camera by Delegates.notNull<OrthographicCamera>()
    private val uiCamera = OrthographicCamera()
    private var viewport by Delegates.notNull<Viewport>()
    private val engine = Engine()
    private var transitionOut = false
    private var transitionIn = true
    private val transitionFrom = Vector2()
    private var transitionRadius = 0f
    private var finished = false
    private var totalCoins = 0
    private var collectedCoins = 0
    private var rotation = 0f
    private val lastCameraPos = Vector2()
    private var lastWallHit = 0f
    private var lastCoin = 0f
    private var musicMuted = false

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
        engine.addSystem(AnimationSystem())
        engine.addSystem(ScriptSystem())
        engine.addSystem(RenderSystem(viewport))

        map.using {
            val levelLayer = it.layers.get("level") ?: throw IllegalStateException("Layer 'level' not found.")

            totalCoins = levelLayer.objects.filter { it.properties.containsKey("type") && it.properties["type"] == Entities.TYPE_COIN }.count()
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
        Events.coin = { coin, player -> onCoin(coin, player) }
        Events.wall = { wall, player -> onWall(wall, player) }
    }

    private fun onWall(wall: Entity, player: Entity) {
        val box2d = player.getComponent(Box2DComponent::class.java)
        if (box2d != null && box2d.body != null) {
            val speed = box2d.body!!.linearVelocity.len()
            if (speed > 10f && (TimeKeeper.totalTime - lastWallHit) > .2f) {
                lastWallHit = TimeKeeper.totalTime
                if (!Sounds.soundMuted) Sounds.bounce.play(.5f, 1f + (MathUtils.random(-.2f, .3f)), 0f)
            }
        }
    }

    private var lastPitch = 0f

    private fun onCoin(coin: Entity, player: Entity) {
        engine.removeEntity(coin)
        collectedCoins++

        if ((TimeKeeper.totalTime - lastCoin) > .1f) {
            var pitch = 1f + (MathUtils.random(-.2f, .3f))
            if ((TimeKeeper.totalTime - lastCoin) < .5f && lastPitch != 0f) {
                pitch = Math.min(lastPitch + .1f, 1.8f)
            }

            if (!Sounds.soundMuted) Sounds.coin.play(.5f, pitch, 0f)

            lastPitch = pitch
            lastCoin = TimeKeeper.totalTime
        }
    }

    private fun onDead() {
        if (!Sounds.soundMuted) Sounds.dead.play(.8f, 1f + (MathUtils.random(-.2f, .3f)), 0f)
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
            if (Gdx.input.isKeyPressed(Input.Keys.M)) {
                musicMuted = !musicMuted
                if (!musicMuted) Sounds.music.play() else Sounds.music.stop()
            }

            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                Sounds.soundMuted = !Sounds.soundMuted
            }

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
                rotation = (360f + (rotation + angle)) % 360f
                engine.getSystem(RenderSystem::class.java).rotation = rotation

                val system = engine.getSystem(Box2DSystem::class.java)
                system.world.gravity = system.world.gravity.rotate(angle)
                camera.rotate(angle, 0f, 0f, 1f)
            }
        }
    }

    override fun update() {
        // Kill camera jitter
        val player = engine.getSystem(TagSystem::class.java)["player"]
        if (player != null) {
            val box2d = player.getComponent(Box2DComponent::class.java)
            if (box2d != null && box2d.body != null) {
                val position = box2d.body!!.position
                val dst = position.dst(lastCameraPos)
                if (dst > .1f) {
                    camera.position.set(position, 0f)
                    lastCameraPos.set(position)
                }
            }
        }

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
        } else {
            drawUI()
        }
    }

    private fun drawUI() {
        if (totalCoins > 0) {
            batch.projectionMatrix = uiCamera.combined
            batch.begin()
            Fonts.font32.draw(batch, "Coins: $collectedCoins/$totalCoins", 8f, 32f)
            batch.end()
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
