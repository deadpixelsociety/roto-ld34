package com.thedeadpixelsociety.ld34.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.thedeadpixelsociety.ld34.GameServices
import com.thedeadpixelsociety.ld34.components.*
import java.util.*

class RenderSystem(private val viewport: Viewport) : SortedIteratingSystem(RenderSystem.FAMILY, ZOrderComparator()) {
    companion object {
        val FAMILY = Family.all(BoundsComponent::class.java, TransformComponent::class.java, RenderComponent::class.java).get()
    }

    private val boundsMapper = ComponentMapper.getFor(BoundsComponent::class.java)
    private val renderMapper = ComponentMapper.getFor(RenderComponent::class.java)
    private val tintMapper = ComponentMapper.getFor(TintComponent::class.java)
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val renderer by lazy { GameServices[ShapeRenderer::class] }
    private var shadows = false
    var angle = 0f
        get
        set(value) {
            field = value
            shadowOffset.rotate(value)
        }
    var shadowOffset = Vector2(2.5f, -2.5f)

    override fun checkProcessing() = false

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val bounds = boundsMapper.get(entity)!!
        val render = renderMapper.get(entity)!!
        val transform = transformMapper.get(entity)!!
        val tint = tintMapper.get(entity)

        val color = Color(tint?.color ?: Color.WHITE)
        color.a = .5f
        renderer.color = color

        when (render.shape) {
            RenderShape.CIRCLE -> {
                renderer.circle(transform.position.x,
                        transform.position.y,
                        bounds.rect.width * .5f, 64)
            }
            RenderShape.RECTANGLE -> {
                renderer.rect(transform.position.x + (if (shadows) shadowOffset.x else 0f) - bounds.rect.width * .5f,
                        transform.position.y + (if (shadows) shadowOffset.y else 0f) - bounds.rect.height * .5f,
                        transform.position.x,
                        transform.position.y,
                        bounds.rect.width,
                        bounds.rect.height,
                        transform.scale.x,
                        transform.scale.y,
                        transform.rotation)
            }
            RenderShape.POLYGON -> {
                throw UnsupportedOperationException()
            }
            RenderShape.LINE -> {
                renderer.line(transform.position.x + (if (shadows) shadowOffset.x else 0f), transform.position.y + (if (shadows) shadowOffset.y else 0f), bounds.rect.width, bounds.rect.height)
            }
        }


        renderer.color = Color.WHITE
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
    }

    private fun begin() {
        viewport.apply()
        renderer.projectionMatrix = viewport.camera.combined
        renderer.begin(ShapeRenderer.ShapeType.Filled)
    }

    private fun end() {
        renderer.end()
    }
}

class ZOrderComparator : Comparator<Entity> {
    override fun compare(o1: Entity?, o2: Entity?): Int {
        val r1 = o1?.getComponent(RenderComponent::class.java)
        val r2 = o2?.getComponent(RenderComponent::class.java)

        return if (r1 != null && r2 != null) r1.zOrder.compareTo(r2.zOrder) else 0
    }
}
