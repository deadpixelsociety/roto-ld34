package com.thedeadpixelsociety.ld34.scripts

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.thedeadpixelsociety.ld34.Events
import com.thedeadpixelsociety.ld34.TimeKeeper
import com.thedeadpixelsociety.ld34.components.Box2DComponent
import com.thedeadpixelsociety.ld34.components.TransformComponent
import com.thedeadpixelsociety.ld34.systems.TagSystem

class WallScript() : Script {
    companion object {
        const val HIT_DELAY = .5f
    }

    private var hit = false
    private var lastHit = 0f
    private val proj = Vector2()

    override fun act(engine: Engine, entity: Entity) {
        val box2d = entity.getComponent(Box2DComponent::class.java)
        if (box2d != null && box2d.collision) {
            val player = engine.getSystem(TagSystem::class.java)["player"]

            if (!hit && player != null && box2d.collided == player && (TimeKeeper.totalTime - lastHit) > HIT_DELAY) {
                hit = true
                lastHit = TimeKeeper.totalTime
                Events.wall(entity, player)
            }
        }

        // Box2d says we're longer colliding, but we don't want to end the 'hit' until we're at least a certain
        // distance apart to eliminate repeat events
        if (hit && box2d.collision == false) {
            val player = engine.getSystem(TagSystem::class.java)["player"]
            if (player != null) {
                val transform = entity.getComponent(TransformComponent::class.java)
                val playerTransform = player.getComponent(TransformComponent::class.java)

                val a = playerTransform.position
                val b = box2d.lastNormal

                val dot = a.dot(b)
                proj.set(dot * b.x, dot * b.y)

                if (proj.x != 0f) {
                    proj.sub(transform.position.x, 0f)
                } else if (proj.y != 0f) {
                    proj.sub(0f, transform.position.y)
                }

                val dist = proj.len()

                if (dist > 17.5f) {
                    hit = false
                }
            }
        }
    }
}
