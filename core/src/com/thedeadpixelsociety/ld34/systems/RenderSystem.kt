package com.thedeadpixelsociety.ld34.systems

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
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

    override fun checkProcessing() = false

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val bounds = boundsMapper.get(entity)!!
        val render = renderMapper.get(entity)!!
        val transform = transformMapper.get(entity)!!
        val tint = tintMapper.get(entity)

        renderer.color = tint?.color ?: Color.WHITE

        when (render.shape) {
            RenderShape.CIRCLE -> {
                renderer.circle(transform.position.x, transform.position.y, bounds.rect.width * .5f)
            }
            RenderShape.RECTANGLE -> {
                renderer.rect(transform.position.x - bounds.rect.width * .5f,
                        transform.position.y - bounds.rect.height * .5f,
                        bounds.rect.width,
                        bounds.rect.height)
            }
            RenderShape.POLYGON -> {
                throw UnsupportedOperationException()
            }
        }


        renderer.color = Color.WHITE
    }

    override fun update(deltaTime: Float) {
        begin()
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
