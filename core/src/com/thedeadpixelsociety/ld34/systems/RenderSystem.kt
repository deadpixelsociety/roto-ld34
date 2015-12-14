package com.thedeadpixelsociety.ld34.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.thedeadpixelsociety.ld34.Fonts
import com.thedeadpixelsociety.ld34.GameServices
import com.thedeadpixelsociety.ld34.components.*
import com.thedeadpixelsociety.ld34.has
import java.util.*

class RenderSystem(private val viewport: Viewport) : SortedIteratingSystem(RenderSystem.FAMILY, ZOrderComparator()) {
    companion object {
        val FAMILY = Family.all(BoundsComponent::class.java, TransformComponent::class.java, RenderComponent::class.java).get()
    }

    private val animationMapper = ComponentMapper.getFor(AnimationComponent::class.java)
    private val boundsMapper = ComponentMapper.getFor(BoundsComponent::class.java)
    private val renderMapper = ComponentMapper.getFor(RenderComponent::class.java)
    private val tintMapper = ComponentMapper.getFor(TintComponent::class.java)
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val tagMapper = ComponentMapper.getFor(TagComponent::class.java)
    private val box2DMapper = ComponentMapper.getFor(Box2DComponent::class.java)
    private val groupMapper = ComponentMapper.getFor(GroupComponent::class.java)
    private val textMapper = ComponentMapper.getFor(TextComponent::class.java)
    private val renderer by lazy { GameServices[ShapeRenderer::class] }
    private val batch by lazy { GameServices[SpriteBatch::class] }
    private var shadows = false
    var rotation = 0f
    var angle = 0f
        get() = field
        set(value) {
            field = value
            shadowOffset.rotate(value)
        }
    var shadowOffset = Vector2(2.5f, -2.5f)

    override fun checkProcessing() = false

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val bounds = boundsMapper.get(entity)
        val render = renderMapper.get(entity)
        val transform = transformMapper.get(entity)
        val tint = tintMapper.get(entity)
        val tag = tagMapper.get(entity)
        val group = groupMapper.get(entity)
        val box2d = box2DMapper.get(entity)
        val text = textMapper.get(entity)

        val color = Color(tint?.color ?: Color.WHITE)
        color.a = .5f
        renderer.color = color
        val isText = group != null && group.group == "text" && text != null

        if (!isText) {
            var radius = bounds.rect.width * .5f

            val shadowExclude = (tag != null && tag.tag == "player") || (group != null && group.group == "coin")

            if (!shadows || (shadows && !shadowExclude)) {
                when (render.type) {
                    RenderType.CIRCLE -> {
                        if (tag != null && tag.tag == "player" && box2d != null && box2d.body != null) {
                            val velocity = box2d.body!!.linearVelocity

                            val r2 = radius * 2f
                            val t = velocity.len() / 175f
                            val tmp = Vector2(velocity).nor()
                            var rx = (r2 + ((r2 * .5f * t) * Math.abs(tmp.x)))
                            var ry = (r2 + ((r2 * .5f * t) * Math.abs(tmp.y)))

                            renderer.ellipse(transform.position.x - radius, transform.position.y - radius, rx, ry, 64)
                        } else {
                            renderer.circle(transform.position.x, transform.position.y, radius * transform.scale.x, 64)
                        }
                    }
                    RenderType.RECTANGLE -> {
                        renderer.rect(transform.position.x + (if (shadows) shadowOffset.x else 0f) - bounds.rect.width * .5f,
                                transform.position.y + (if (shadows) shadowOffset.y else 0f) - bounds.rect.height * .5f,
                                bounds.rect.width * .5f,
                                bounds.rect.height * .5f,
                                bounds.rect.width,
                                bounds.rect.height,
                                transform.scale.x,
                                transform.scale.y,
                                transform.rotation)
                    }
                    RenderType.POLYGON -> {
                        throw UnsupportedOperationException()
                    }
                    RenderType.LINE -> {
                        renderer.line(transform.position.x + (if (shadows) shadowOffset.x else 0f), transform.position.y + (if (shadows) shadowOffset.y else 0f), bounds.rect.width, bounds.rect.height)
                    }
                    else -> {
                    }
                }
            }

            renderer.color = Color.WHITE
        }
    }

    override fun update(deltaTime: Float) {
        Gdx.gl20.glEnable(GL20.GL_BLEND)
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        begin()
        shadows = true
        super.update(deltaTime)
        end()
        Gdx.gl20.glDisable(GL20.GL_BLEND)
        begin()
        shadows = false
        super.update(deltaTime)
        end()

        beginBatch()
        entities.filter { it.has(TextComponent::class.java) }.forEach {
            val bounds = boundsMapper.get(it)
            val transform = transformMapper.get(it)
            val text = textMapper.get(it)

            Fonts.font32.draw(batch,
                    text.text,
                    transform.position.x - bounds.rect.width * .5f,
                    transform.position.y - bounds.rect.height * .5f + Fonts.font32.lineHeight)
        }
        endBatch()
    }

    private fun begin() {
        viewport.apply()
        renderer.projectionMatrix = viewport.camera.combined
        renderer.begin(ShapeRenderer.ShapeType.Filled)
    }

    private fun end() {
        renderer.end()
    }

    private fun beginBatch() {
        batch.projectionMatrix = viewport.camera.combined
        batch.begin()
    }

    private fun endBatch() {
        batch.end()
    }
}

class ZOrderComparator : Comparator<Entity> {
    override fun compare(o1: Entity?, o2: Entity?): Int {
        val r1 = o1?.getComponent(RenderComponent::class.java)
        val r2 = o2?.getComponent(RenderComponent::class.java)

        return if (r1 != null && r2 != null) r1.zOrder.compareTo(r2.zOrder) else 0
    }
}
